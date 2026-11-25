package com.kazemieh.fintrack


import android.app.Application
import com.kazemieh.designsystem.AppLanguage
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import java.util.Locale
import com.kazemieh.composeApp.initKoin
import com.kazemieh.common.analytics.CrashReporter
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.fixed_expense.FixedExpenseWorker
import com.kazemieh.sync.SyncWorker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class FinTrackApplication : Application() {

    private val notificationManager: NotificationManager by inject()
    private val crashReporter: CrashReporter by inject()
    private val preferenceUseCases: PreferenceUseCases by inject()

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@FinTrackApplication)
            modules(com.kazemieh.fintrack.di.appModule)
        }

        // Workers and channel creation may run before Compose applies the saved app language.
        val language = AppLanguage.fromLanguageTag(preferenceUseCases.getStringPreference(
            FinTrackPreferences.PREF_LANGUAGE, AppLanguage.PERSIAN.languageTag
        ))
        Locale.setDefault(Locale.forLanguageTag(language.languageTag))

        crashReporter.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashReporter.setCustomKey("distribution", BuildConfig.FLAVOR)
        crashReporter.setCustomKey("is_debug", BuildConfig.DEBUG.toString())
        crashReporter.log("application_initialized")

        notificationManager.createChannels()
        SyncWorker.enqueuePeriodicWork(this)
        FixedExpenseWorker.enqueuePeriodicWork(this)
        com.kazemieh.sms_reader.IgnoredSmsCleanupWorker.enqueuePeriodicWork(this)
    }
}
