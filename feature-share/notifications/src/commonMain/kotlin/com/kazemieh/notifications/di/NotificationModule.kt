package com.kazemieh.notifications.di

import com.kazemieh.notifications.ui.NotificationSettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect fun notificationPlatformModule(): Module

val notificationModule = module {
    includes(notificationPlatformModule())

    viewModel {
        NotificationSettingsViewModel(
            analytics = get(),
            preferenceUseCases = get(),
            notificationManager = get()
        )
    }
}
