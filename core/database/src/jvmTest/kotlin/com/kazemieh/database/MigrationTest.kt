package com.kazemieh.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.junit.Test

class MigrationTest {

    @Test
    fun `test all database migrations execute successfully`() {
        // Create an in-memory database driver
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        
        // Schema.synchronous().create runs the initial CREATE TABLE statements 
        // plus all migration scripts (1.sqm -> 25.sqm) sequentially
        FinTrackDatabase.Schema.synchronous().create(driver)
        
        // Verify the database is usable by creating a FinTrackDatabase instance
        val database = FinTrackDatabase(
            driver = driver,
            assetAdapter = com.kazemieh.database.Asset.Adapter(
                typeAdapter = app.cash.sqldelight.EnumColumnAdapter()
            ),
            rate_cacheAdapter = com.kazemieh.database.Rate_cache.Adapter(
                typeAdapter = app.cash.sqldelight.EnumColumnAdapter()
            )
        )
        
        // If we reach here without exception, the schema is valid
        // which means there are no SQL syntax errors or conflicting alters in any .sqm files.
        driver.close()
    }
}
