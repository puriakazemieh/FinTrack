package com.kazemieh.notifications

import kotlinx.datetime.LocalDateTime

class IosNotificationScheduler : NotificationScheduler {
    override fun scheduleReminder(id: String, title: String, message: String, scheduledTime: LocalDateTime, channelId: String) {}
    override fun cancelReminder(id: String) {}
}
