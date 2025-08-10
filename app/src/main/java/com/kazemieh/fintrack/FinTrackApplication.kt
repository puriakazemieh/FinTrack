package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.di.databaseModule
import com.kazemieh.domain.di.domainModule
import com.kazemieh.transaction.di.addTransactionPresentationModule
import com.kazemieh.transaction.di.transactionPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class FinTrackApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        val appModule = listOf(
            dataModule,
            domainModule,
            databaseModule,
            transactionPresentationModule,
            addTransactionPresentationModule

        )
        startKoin {
            androidContext(this@FinTrackApplication)
            modules(appModule)

        }

    }
}