package com.kazemieh.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.ObservableSettings
import org.koin.dsl.module

val preferencesModule = module {

    single<Settings> { Settings() }

    single { FinTrackPreferences(get()) }

    single { SettingsObserver(get<Settings>() as ObservableSettings) }
}
