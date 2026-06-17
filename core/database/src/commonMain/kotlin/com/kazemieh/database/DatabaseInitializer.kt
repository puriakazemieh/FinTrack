package com.kazemieh.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.kazemieh.database.FinTrackDatabase.Companion.Schema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class DatabaseInitializer(private val driver: SqlDriver) {
    suspend fun initialize() = withContext(Dispatchers.Default) {
        try {
            Schema.awaitCreate(driver)
        } catch (e: Exception) {
            // Table already exists, ensure parentId column exists for migration
            try {
                val columnExists = driver.executeQuery(
                    identifier = null,
                    sql = "PRAGMA table_info(category)",
                    mapper = { cursor ->
                        var found = false
                        while (cursor.next().value) {
                            if (cursor.getString(1) == "parentId") {
                                found = true
                                break
                            }
                        }
                        QueryResult.Value(found)
                    },
                    parameters = 0
                ).await()

                if (!columnExists) {
                    driver.execute(
                        identifier = null,
                        sql = "ALTER TABLE category ADD COLUMN parentId INTEGER REFERENCES category(id)",
                        parameters = 0
                    ).await()
                }
            } catch (ex: Exception) {
                println("Migration check failed: ${ex.message}")
            }
        }
    }
}