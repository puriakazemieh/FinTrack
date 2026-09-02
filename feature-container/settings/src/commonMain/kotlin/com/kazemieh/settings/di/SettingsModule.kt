package com.kazemieh.settings.di

import com.kazemieh.settings.CurrencySettingsViewModel
import com.kazemieh.settings.ManageToolsViewModel
import com.kazemieh.settings.ProfileEditViewModel
import com.kazemieh.settings.ProfileViewModel
import com.kazemieh.settings.ThemeSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { ThemeSettingsViewModel(get(), get()) }
    viewModel { CurrencySettingsViewModel(get(), get()) }
    viewModel { ProfileEditViewModel(get(), get()) }
    viewModel { ManageToolsViewModel(get(), get()) }
}
