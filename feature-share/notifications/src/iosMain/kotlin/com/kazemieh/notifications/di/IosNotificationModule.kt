package com.kazemieh.notifications.di

import com.kazemieh.notifications.IosNotificationManager
import com.kazemieh.notifications.IosNotificationScheduler
import com.kazemieh.notifications.NotificationManager
import com.kazemieh.notifications.NotificationScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun notificationPlatformModule(): Module = module {
    single<NotificationManager> { IosNotificationManager() }
    single<NotificationScheduler> { IosNotificationScheduler() }
}
