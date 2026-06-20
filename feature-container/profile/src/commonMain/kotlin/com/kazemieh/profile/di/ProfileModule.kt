package com.kazemieh.profile.di

import com.kazemieh.profile.CurrencySettingsViewModel
import com.kazemieh.profile.ProfileEditViewModel
import com.kazemieh.profile.ProfileViewModel
import com.kazemieh.profile.ThemeAndCurrencyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { ProfileViewModel(get()) }
    viewModel { ThemeAndCurrencyViewModel(get()) }
    viewModel { CurrencySettingsViewModel(get()) }
    viewModel { ProfileEditViewModel(get()) }
}
