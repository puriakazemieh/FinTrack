package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.composeApp.initKoin
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.fixed_expense.FixedExpenseWorker
import com.kazemieh.sync.SyncWorker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class FinTrackApplication : Application() {

    private val notificationManager: NotificationManager by inject()

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@FinTrackApplication)
        }

        notificationManager.createChannels()
        SyncWorker.enqueuePeriodicWork(this)
        FixedExpenseWorker.enqueuePeriodicWork(this)
    }
}
