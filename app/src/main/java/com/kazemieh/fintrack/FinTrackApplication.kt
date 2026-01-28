package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.category.di.transactionAddCategoryModule
import com.kazemieh.category.di.transactionCategoryModule
import com.kazemieh.category.di.transactionDeleteCategoryModule
import com.kazemieh.dashboard.di.dashboardModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.di.databaseModule
import com.kazemieh.domain.di.domainModule
import com.kazemieh.financialsource.di.deleteSourceModule
import com.kazemieh.financialsource.di.transactionAddFinancialSourceModule
import com.kazemieh.financialsource.di.transactionFinancialSourceModule
import com.kazemieh.person.di.transactionAddPersonModule
import com.kazemieh.person.di.transactionDeletePersonModule
import com.kazemieh.person.di.transactionPersonModule
import com.kazemieh.setting.di.settingModule
import com.kazemieh.tag.di.transactionAddTagModule
import com.kazemieh.tag.di.transactionDeleteTagModule
import com.kazemieh.tag.di.transactionTagModule
import com.kazemieh.transaction.di.addTransactionPresentationModule
import com.kazemieh.transaction.di.transactionDeleteViewModelModule
import com.kazemieh.transaction.di.transactionPresentationModule
import com.kazemieh.transaction.di.transactionReportViewModelModule
import com.tosantechno.filter.di.reportViewModel
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
            transactionReportViewModelModule,
            transactionDeleteViewModelModule,
            transactionCategoryModule,
            transactionDeleteCategoryModule,
            transactionAddCategoryModule,
            transactionFinancialSourceModule,
            deleteSourceModule,
            transactionAddFinancialSourceModule,
            transactionAddTagModule,
            transactionDeleteTagModule,
            transactionTagModule,
            settingModule,
            reportViewModel,
            transactionPersonModule,
            transactionDeletePersonModule,
            transactionAddPersonModule,

            dashboardModule,

        )
        startKoin {
            androidContext(this@FinTrackApplication)
            modules(appModule)

        }

    }
}