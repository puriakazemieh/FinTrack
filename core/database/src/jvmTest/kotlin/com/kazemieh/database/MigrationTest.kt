package com.kazemieh.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class MigrationTest {

    @Test
    fun `test all database migrations execute successfully`() = runBlocking {
        // Create an in-memory database driver
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        
        // This will run all migration scripts sequentially (1.sqm -> 25.sqm)
        FinTrackDatabase.Schema.migrate(
            driver = driver,
            oldVersion = 0,
            newVersion = FinTrackDatabase.Schema.version
        )
        
        // Verify the database is usable
        val database = FinTrackDatabase(
            driver = driver,
            assetAdapter = com.kazemieh.database.Asset.Adapter(
                typeAdapter = app.cash.sqldelight.EnumColumnAdapter()
            ),
            rate_cacheAdapter = com.kazemieh.database.Rate_cache.Adapter(
                typeAdapter = app.cash.sqldelight.EnumColumnAdapter()
            )
        )
        
        // If we reach here without exception, migrations have successfully completed
        // which means there are no SQL syntax errors or conflicting alters in any .sqm files.
        driver.close()
    }
}
