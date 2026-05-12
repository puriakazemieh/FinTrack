package com.kazemieh.database

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform

actual fun Scope.createDriver(): SqlDriver {
    val context = KoinPlatform.getKoin().get<Context>()
    return AndroidSqliteDriver(
        schema = FinTrackDatabase.Schema.synchronous(),
        context = context,
        name = "fin_track.db"
    )
}
