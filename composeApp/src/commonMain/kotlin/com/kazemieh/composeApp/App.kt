package com.kazemieh.composeApp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kazemieh.asset.di.assetModule
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
import com.kazemieh.shopping.di.shoppingModule
import com.kazemieh.notes.di.notesModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.database.DatabaseInitializer
import com.kazemieh.database.di.databaseModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FintrackTheme
import com.kazemieh.designsystem.LocalCurrency
import com.kazemieh.designsystem.ThemeMode
import com.kazemieh.domain.di.domainModule
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.debt.di.debtModule
import com.kazemieh.financialsource.di.deleteSourceModule
import com.kazemieh.financialsource.di.transactionAddFinancialSourceModule
import com.kazemieh.financialsource.di.transactionFinancialSourceModule
import com.kazemieh.lock.LockGate
import com.kazemieh.lock.di.lockModule
import com.kazemieh.money.Currency
import com.kazemieh.network.di.networkModule
import com.kazemieh.person.di.transactionAddPersonModule
import com.kazemieh.person.di.transactionDeletePersonModule
import com.kazemieh.person.di.transactionPersonModule
import com.kazemieh.preferences.FinTrackPreferences
import com.kazemieh.preferences.preferencesModule
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
import com.kazemieh.backup_export.di.backupExportModule
import com.kazemieh.sync.di.syncModule
import com.kazemieh.composeApp.navigation.Screen
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import kotlin.time.Clock

@Composable
fun App() {
    val initializer = koinInject<DatabaseInitializer>()
    val preferenceUseCases = koinInject<PreferenceUseCases>()

    var isReady by remember { mutableStateOf(false) }

    val currentTheme by preferenceUseCases.getStringFlow(
        FinTrackPreferences.PREF_THEME,
        AppTheme.GLASS_DARK.name
    ).collectAsState(AppTheme.GLASS_DARK.name)

    val themeMode by preferenceUseCases.getStringFlow(
        FinTrackPreferences.PREF_THEME_MODE,
        ThemeMode.MANUAL.name
    ).collectAsState(ThemeMode.MANUAL.name)

    val themeStartTime by preferenceUseCases.getStringFlow(
        FinTrackPreferences.PREF_THEME_START_TIME,
        "20:00"
    ).collectAsState("20:00")

    val themeEndTime by preferenceUseCases.getStringFlow(
        FinTrackPreferences.PREF_THEME_END_TIME,
        "07:00"
    ).collectAsState("07:00")

    val currentCurrency by preferenceUseCases.getStringFlow(
        FinTrackPreferences.PREF_CURRENCY,
        Currency.TOMAN.name
    ).collectAsState(Currency.TOMAN.name)

    val isSystemDark = isSystemInDarkTheme()

    val calculatedTheme = remember(currentTheme, themeMode, isSystemDark, themeStartTime, themeEndTime) {
        val theme = try {
            AppTheme.valueOf(currentTheme)
        } catch (e: Exception) {
            AppTheme.GLASS_DARK
        }
        val mode = try {
            ThemeMode.valueOf(themeMode)
        } catch (e: Exception) {
            ThemeMode.MANUAL
        }

        if (mode == ThemeMode.MANUAL) {
            theme
        } else {
            val shouldBeDark = when (mode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.SUNRISE_SUNSET -> isTimeInRange("18:00", "06:00")
                ThemeMode.CUSTOM_TIME -> isTimeInRange(themeStartTime, themeEndTime)
                else -> theme.isDark
            }
            if (shouldBeDark) theme.toDark() else theme.toLight()
        }
    }

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
            FintrackTheme(theme = calculatedTheme) {
                LockGate {
                    FinTrackHost(startDestination = startDestination)
                }
            }
        }
    }
}

private fun isTimeInRange(start: String, end: String): Boolean {
    return try {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        val startParts = start.split(":").map { it.toInt() }
        val endParts = end.split(":").map { it.toInt() }

        val startTime = LocalTime(startParts[0], startParts[1])
        val endTime = LocalTime(endParts[0], endParts[1])
        val currentTime = LocalTime(now.hour, now.minute)

        if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            currentTime >= startTime || currentTime <= endTime
        }
    } catch (e: Exception) {
        false
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
        fixedExpenseModule,
        shoppingModule,
        notesModule,
        networkModule,
        assetModule,
        backupExportModule,
        syncModule
    )
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }

}