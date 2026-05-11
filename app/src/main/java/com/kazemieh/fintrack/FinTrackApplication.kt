package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.composeApp.initKoin
import org.koin.android.ext.koin.androidContext

class FinTrackApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@FinTrackApplication)
        }
    }
}