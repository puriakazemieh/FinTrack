package com.kazemieh.notifications.di

import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.notifications.NotificationManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun notificationPlatformModule(): Module = module {
    single<NotificationManager> { 
        object : NotificationManager {
            override fun createChannels() {}
            override fun createChannel(id: String, name: String, importance: Int) {}
            override fun showNotification(id: Int, title: String, message: String, channelId: String) {}
            override fun hasPermission(): Boolean = true
            override fun openSettings() {}
        }
    }
    single<NotificationScheduler> {
        object : NotificationScheduler {
            override fun scheduleReminder(id: String, title: String, message: String, scheduledTime: kotlinx.datetime.LocalDateTime, channelId: String) {}
            override fun cancelReminder(id: String) {}
        }
    }
}
