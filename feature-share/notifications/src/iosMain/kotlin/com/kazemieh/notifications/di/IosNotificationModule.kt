package com.kazemieh.notifications.di

import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.notifications.IosNotificationManager
import com.kazemieh.notifications.IosNotificationScheduler
import com.kazemieh.domain.notification.NotificationManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun notificationPlatformModule(): Module = module {
    single<NotificationManager> { IosNotificationManager() }
    single<NotificationScheduler> { IosNotificationScheduler() }
}
