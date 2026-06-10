package com.kazemieh.notifications.di

import com.kazemieh.notifications.AndroidNotificationManager
import com.kazemieh.notifications.AndroidNotificationScheduler
import com.kazemieh.notifications.NotificationManager
import com.kazemieh.notifications.NotificationScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun notificationPlatformModule(): Module = module {
    single<NotificationManager> { AndroidNotificationManager(get()) }
    single<NotificationScheduler> { AndroidNotificationScheduler(get()) }
}
