package com.kazemieh.composeApp

import androidx.compose.runtime.Composable
import com.kazemieh.category.di.transactionAddCategoryModule
import com.kazemieh.category.di.transactionCategoryModule
import com.kazemieh.category.di.transactionDeleteCategoryModule
import com.kazemieh.dashboard.di.dashboardModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.di.databaseModule
import com.kazemieh.designsystem.FintrackTheme
import com.kazemieh.domain.di.domainModule
import com.kazemieh.filter.di.reportViewModel
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
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@Composable
fun App() {
    FintrackTheme {
        FinTrackHost()
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
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
        config?.invoke(this)
        modules(appModule)
    }

}