package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.backup.di.backupViewModelModule
import com.kazemieh.category.di.transactionAddCategoryModule
import com.kazemieh.category.di.transactionCategoryModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.di.databaseModule
import com.kazemieh.domain.di.domainModule
import com.kazemieh.financialsource.di.transactionAddFinancialSourceModule
import com.kazemieh.financialsource.di.transactionFinancialSourceModule
import com.kazemieh.tag.di.transactionAddTagModule
import com.kazemieh.tag.di.transactionTagModule
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
            addTransactionPresentationModule,
            transactionCategoryModule,
            transactionAddCategoryModule,
            transactionFinancialSourceModule,
            transactionAddFinancialSourceModule,
            transactionAddTagModule,
            transactionTagModule,
            backupViewModelModule

        )
        startKoin {
            androidContext(this@FinTrackApplication)
            modules(appModule)

        }

    }
}