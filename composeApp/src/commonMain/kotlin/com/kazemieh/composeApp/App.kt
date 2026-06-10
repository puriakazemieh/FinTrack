package com.kazemieh.composeApp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kazemieh.budget.di.budgetModule
import com.kazemieh.category.di.transactionAddCategoryModule
import com.kazemieh.category.di.transactionCategoryModule
import com.kazemieh.category.di.transactionDeleteCategoryModule
import com.kazemieh.check.di.checkModule
import com.kazemieh.common.di.commonModule
import com.kazemieh.dashboard.di.dashboardModule
import com.kazemieh.onboarding.di.onboardingModule
import com.kazemieh.notifications.di.notificationModule
import com.kazemieh.installment.di.installmentModule
import com.kazemieh.fixed_expense.di.fixedExpenseModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.DatabaseInitializer
import com.kazemieh.database.di.databaseModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FintrackTheme
import com.kazemieh.designsystem.LocalCurrency
import com.kazemieh.domain.di.domainModule
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.debt.di.debtModule
import com.kazemieh.financialsource.di.deleteSourceModule
import com.kazemieh.financialsource.di.transactionAddFinancialSourceModule
import com.kazemieh.financialsource.di.transactionFinancialSourceModule
import com.kazemieh.lock.LockGate
import com.kazemieh.lock.di.lockModule
import com.kazemieh.money.Currency
import com.kazemieh.person.di.transactionAddPersonModule
import com.kazemieh.person.di.transactionDeletePersonModule
import com.kazemieh.person.di.transactionPersonModule
import com.kazemieh.preferences.preferencesModule
import com.kazemieh.profile.ThemeAndCurrencyViewModel
import com.kazemieh.profile.di.profileModule
import com.kazemieh.search.di.searchModule
import com.kazemieh.storage.storageModule
import com.kazemieh.tag.di.transactionAddTagModule
import com.kazemieh.tag.di.transactionDeleteTagModule
import com.kazemieh.tag.di.transactionTagModule
import com.kazemieh.tools.di.toolsModule
import com.kazemieh.transaction.di.addTransactionPresentationModule
import com.kazemieh.transaction.di.transactionDeleteViewModelModule
import com.kazemieh.transaction.di.transactionPresentationModule
import com.kazemieh.transaction.di.transactionReportViewModelModule
import com.kazemieh.transactions.di.transactionsViewModelModule
import com.kazemieh.composeApp.navigation.Screen
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

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
        val isFirstRun = preferenceUseCases.getBooleanPreference(
            com.kazemieh.domain.usecase.SeedDataUseCase.PREF_IS_FIRST_RUN,
            true
        )
        val startDestination = if (isFirstRun) Screen.Onboarding else Screen.BottomBarGraph

        CompositionLocalProvider(LocalCurrency provides Currency.valueOf(currentCurrency)) {
            FintrackTheme(theme = AppTheme.valueOf(currentTheme)) {
                LockGate {
                    FinTrackHost(startDestination = startDestination)
                }
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
        debtModule,
        transactionDeletePersonModule,
        transactionAddPersonModule,
        toolsModule,
        dashboardModule,
        onboardingModule,
        transactionsViewModelModule,
        searchModule,
        preferencesModule,
        lockModule,
        notificationModule,
        budgetModule,
        installmentModule,
        checkModule,
        fixedExpenseModule
    )
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }

}