package com.kazemieh.notifications

import kotlinx.datetime.LocalDateTime

interface NotificationScheduler {
    fun scheduleReminder(
        id: String,
        title: String,
        message: String,
        scheduledTime: LocalDateTime,
        channelId: String
    )
    fun cancelReminder(id: String)
}
