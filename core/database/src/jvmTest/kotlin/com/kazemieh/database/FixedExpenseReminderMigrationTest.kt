package com.kazemieh.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.nio.file.Files
import java.sql.DriverManager
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FixedExpenseReminderMigrationTest {
    @Test fun upgradeFrom35PreservesLegacyRecordsAndUnrelatedData() {
        val directory = Files.createTempDirectory("fixed-reminder-migration-")
        val url = "jdbc:sqlite:" + directory.resolve("legacy.db")
        val driver = JdbcSqliteDriver(url, Properties().apply { setProperty("foreign_keys", "true") })
        try {
            driver.execute(null, "CREATE TABLE category(id INTEGER PRIMARY KEY)", 0)
            driver.execute(null, "CREATE TABLE source(id INTEGER PRIMARY KEY)", 0)
            driver.execute(null, "INSERT INTO category(id) VALUES(1)", 0)
            driver.execute(null, "INSERT INTO source(id) VALUES(1)", 0)
            driver.execute(null, "CREATE TABLE marker(id INTEGER PRIMARY KEY,value TEXT)", 0)
            driver.execute(null, "INSERT INTO marker VALUES(1,'untouched')", 0)
            driver.execute(null, """
                CREATE TABLE fixed_expense (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL DEFAULT '', amount INTEGER NOT NULL,
                    categoryId INTEGER NOT NULL, sourceId INTEGER NOT NULL,
                    description TEXT, recurrence TEXT NOT NULL,
                    startDate INTEGER NOT NULL, nextDueDate INTEGER NOT NULL, endDate INTEGER,
                    isAutoPostEnabled INTEGER NOT NULL DEFAULT 0,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    updatedAt INTEGER NOT NULL DEFAULT 0, syncStatus INTEGER NOT NULL DEFAULT 0,
                    currencyCode TEXT NOT NULL DEFAULT 'IRT',
                    FOREIGN KEY(categoryId) REFERENCES category(id) ON DELETE CASCADE,
                    FOREIGN KEY(sourceId) REFERENCES source(id) ON DELETE CASCADE
                )
            """.trimIndent(), 0)
            driver.execute(null, "INSERT INTO fixed_expense VALUES(1,'Legacy USD',125,1,1,'Keep','MONTHLY',1700000000000,1701000000000,NULL,1,1,1234,1,'USD')", 0)
            driver.execute(null, "INSERT INTO fixed_expense VALUES(2,'Deleted inactive',25,1,1,NULL,'YEARLY',1690000000000,1710000000000,1800000000000,0,0,5678,2,'IRT')", 0)
            driver.execute(null, "PRAGMA user_version=35", 0)
            val before = rows(url, "fixed_expense")
            val unrelated = listOf("category", "source", "marker", "sqlite_sequence").associateWith { rows(url, it) }
            FinTrackDatabase.Schema.synchronous().migrate(driver, 35, 36)
            val after = rows(url, "fixed_expense")
            assertEquals(before, after.map { it - setOf("reminderEnabled", "reminderTime") })
            assertTrue(after.all { it["reminderEnabled"] == "1" && it["reminderTime"] == null })
            assertEquals(unrelated, unrelated.keys.associateWith { rows(url, it) })
            assertTrue(runCatching {
                driver.execute(null, "INSERT INTO fixed_expense(title,amount,categoryId,sourceId,recurrence,startDate,nextDueDate) VALUES('Invalid',1,999,1,'MONTHLY',0,0)", 0)
            }.isFailure, "Migration must retain foreign-key enforcement")
            assertEquals(after, rows(url, "fixed_expense"))
        } finally {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(directory.resolve("legacy.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }

    private fun rows(url: String, table: String): List<Map<String, String?>> = DriverManager.getConnection(url).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT * FROM \"$table\" ORDER BY rowid").use { result ->
                val columns = (1..result.metaData.columnCount).map { result.metaData.getColumnName(it) }
                buildList { while (result.next()) add(columns.associateWith { result.getString(it) }) }
            }
        }
    }
}
