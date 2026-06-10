package com.kazemieh.domain.notification

interface NotificationScheduler {
    fun scheduleReminder(
        id: String,
        title: String,
        message: String,
        scheduledTime: kotlinx.datetime.LocalDateTime,
        channelId: String
    )
    fun cancelReminder(id: String)
}
