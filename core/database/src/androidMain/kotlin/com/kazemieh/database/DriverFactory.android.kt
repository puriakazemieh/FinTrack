package com.kazemieh.database

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform

actual fun Scope.createDriver(): SqlDriver {
    val context = KoinPlatform.getKoin().get<Context>()

    val oldDb = context.getDatabasePath("fin_track.db")
    if (oldDb.exists()) {
        context.deleteDatabase("fin_track.db")
    }
    return AndroidSqliteDriver(
        schema = FinTrackDatabase.Schema.synchronous(),
        context = context,
        name = "fintrack.db"
    )
}
