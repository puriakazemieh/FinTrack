package com.kazemieh.setting.di

import com.kazemieh.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingModule = module {
    viewModel { SettingViewModel() }
}