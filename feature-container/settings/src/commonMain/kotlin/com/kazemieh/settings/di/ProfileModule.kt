package com.kazemieh.profile.di

import com.kazemieh.profile.CurrencySettingsViewModel
import com.kazemieh.profile.ManageToolsViewModel
import com.kazemieh.profile.ProfileEditViewModel
import com.kazemieh.profile.ProfileViewModel
import com.kazemieh.profile.ThemeSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { ThemeSettingsViewModel(get(), get()) }
    viewModel { CurrencySettingsViewModel(get(), get()) }
    viewModel { ProfileEditViewModel(get(), get()) }
    viewModel { ManageToolsViewModel(get(), get()) }
}
