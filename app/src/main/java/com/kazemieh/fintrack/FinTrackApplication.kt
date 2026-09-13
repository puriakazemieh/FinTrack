package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.composeApp.initKoin
import com.kazemieh.common.analytics.CrashReporter
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.fixed_expense.FixedExpenseWorker
import com.kazemieh.sync.SyncWorker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class FinTrackApplication : Application() {

    private val notificationManager: NotificationManager by inject()
    private val crashReporter: CrashReporter by inject()

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@FinTrackApplication)
            modules(com.kazemieh.fintrack.di.appModule)
        }

        crashReporter.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashReporter.setCustomKey("distribution", BuildConfig.FLAVOR)
        crashReporter.setCustomKey("is_debug", BuildConfig.DEBUG.toString())
        crashReporter.log("application_initialized")

        notificationManager.createChannels()
        SyncWorker.enqueuePeriodicWork(this)
        FixedExpenseWorker.enqueuePeriodicWork(this)
    }
}
