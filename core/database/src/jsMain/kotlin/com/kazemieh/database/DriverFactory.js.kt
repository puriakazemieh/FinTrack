package com.kazemieh.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.koin.core.scope.Scope
import org.w3c.dom.Worker

actual fun Scope.createDriver(): SqlDriver {
    val worker = Worker(
        js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
    )
    return WebWorkerDriver(worker)
}