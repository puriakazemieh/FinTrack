package com.kazemieh.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.scope.Scope
import java.io.File

actual fun Scope.createDriver(): SqlDriver {
    val databasePath = File(System.getProperty("user.home"), ".fintrack/fin_track.db")
    databasePath.parentFile?.mkdirs()

    return JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}").also {
        FinTrackDatabase.Schema.create(it)
    }
}