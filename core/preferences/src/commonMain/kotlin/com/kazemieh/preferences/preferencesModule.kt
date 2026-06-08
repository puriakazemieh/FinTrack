package com.kazemieh.preferences

import com.russhwolf.settings.Settings
import org.koin.dsl.module

val preferencesModule = module {

    single<Settings> { Settings() }

    single { FinTrackPreferences(get()) }
}