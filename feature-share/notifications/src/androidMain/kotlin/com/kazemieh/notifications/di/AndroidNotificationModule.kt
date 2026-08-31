package com.kazemieh.notifications.di

import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.notifications.AndroidNotificationManager
import com.kazemieh.notifications.AndroidNotificationScheduler
import com.kazemieh.domain.notification.NotificationManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun notificationPlatformModule(): Module = module {
    single<NotificationManager> { AndroidNotificationManager(get(), get(), get()) }
    single<NotificationScheduler> { AndroidNotificationScheduler(get()) }
}
