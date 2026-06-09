package com.kazemieh.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.FinTrackDatabase.Companion.Schema
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseInitializer(
    private val database: FinTrackDatabase,
    private val driver: SqlDriver,
) {

    private val _ready = CompletableDeferred<Unit>()
    val ready: Deferred<Unit> = _ready
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

        _ready.complete(Unit)
    }
}