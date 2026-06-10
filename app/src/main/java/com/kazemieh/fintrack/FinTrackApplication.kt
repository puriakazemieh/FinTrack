package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.composeApp.initKoin
import com.kazemieh.notifications.NotificationManager
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
    }
}