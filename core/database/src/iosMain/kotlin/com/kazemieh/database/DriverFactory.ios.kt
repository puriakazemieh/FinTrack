package com.kazemieh.database


import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.scope.Scope

actual fun Scope.createDriver(): SqlDriver {
    return NativeSqliteDriver(
        schema = FinTrackDatabase.Schema,
        name = "fin_track.db"
    )
}