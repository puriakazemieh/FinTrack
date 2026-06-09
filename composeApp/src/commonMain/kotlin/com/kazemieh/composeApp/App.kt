package com.kazemieh.composeApp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kazemieh.category.di.transactionAddCategoryModule
import com.kazemieh.category.di.transactionCategoryModule
import com.kazemieh.category.di.transactionDeleteCategoryModule
import com.kazemieh.common.di.commonModule
import com.kazemieh.storage.storageModule
import com.kazemieh.dashboard.di.dashboardModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.DatabaseInitializer
import com.kazemieh.database.di.databaseModule
import com.kazemieh.designsystem.FintrackTheme
import com.kazemieh.domain.di.domainModule
import com.kazemieh.financialsource.di.deleteSourceModule
import com.kazemieh.financialsource.di.transactionAddFinancialSourceModule
import com.kazemieh.financialsource.di.transactionFinancialSourceModule
import com.kazemieh.person.di.transactionAddPersonModule
import com.kazemieh.person.di.transactionDeletePersonModule
import com.kazemieh.person.di.transactionPersonModule
import com.kazemieh.preferences.preferencesModule
import com.kazemieh.profile.di.profileModule
import com.kazemieh.search.di.searchModule
import com.kazemieh.tag.di.transactionAddTagModule
import com.kazemieh.tag.di.transactionDeleteTagModule
import com.kazemieh.tag.di.transactionTagModule
import com.kazemieh.tools.di.toolsModule
import com.kazemieh.transaction.di.addTransactionPresentationModule
import com.kazemieh.transaction.di.transactionDeleteViewModelModule
import com.kazemieh.transaction.di.transactionPresentationModule
import com.kazemieh.transaction.di.transactionReportViewModelModule
import com.kazemieh.transactions.di.transactionsViewModelModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.money.Currency
import com.kazemieh.designsystem.LocalCurrency
import com.kazemieh.profile.ThemeAndCurrencyViewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState

@Composable
fun App() {
    val initializer = koinInject<DatabaseInitializer>()
    val preferenceUseCases = koinInject<PreferenceUseCases>()

    var isReady by remember { mutableStateOf(false) }

    val currentTheme by preferenceUseCases.getStringFlow(
        ThemeAndCurrencyViewModel.PREF_THEME,
        AppTheme.GLASS_DARK.name
    ).collectAsState(AppTheme.GLASS_DARK.name)

    val currentCurrency by preferenceUseCases.getStringFlow(
        ThemeAndCurrencyViewModel.PREF_CURRENCY,
        Currency.TOMAN.name
    ).collectAsState(Currency.TOMAN.name)

    LaunchedEffect(Unit) {
        initializer.initialize()
        isReady = true
    }

    if (isReady) {
        CompositionLocalProvider(LocalCurrency provides Currency.valueOf(currentCurrency)) {
            FintrackTheme(theme = AppTheme.valueOf(currentTheme)) {
                FinTrackHost()
            }
        }
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    val appModule = listOf(
        commonModule,
        storageModule,
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
        profileModule,
        transactionPersonModule,
        transactionDeletePersonModule,
        transactionAddPersonModule,
        toolsModule,
        dashboardModule,
        transactionsViewModelModule,
        searchModule,
        preferencesModule
        )
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }

}