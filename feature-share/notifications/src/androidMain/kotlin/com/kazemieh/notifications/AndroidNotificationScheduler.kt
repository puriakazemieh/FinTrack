package com.kazemieh.notifications

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.kazemieh.domain.notification.NotificationScheduler
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.Duration
import java.time.LocalDateTime as JavaLocalDateTime

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleReminder(
        id: String,
        title: String,
        message: String,
        scheduledTime: LocalDateTime,
        channelId: String,
        extraId: Long?
    ) {
        val now = JavaLocalDateTime.now()
        val target = scheduledTime.toJavaLocalDateTime()
        val delay = Duration.between(now, target)

        if (delay.isNegative) return

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay)
            .setInputData(
                NotificationWorker.createInputData(
                    title,
                    message,
                    channelId,
                    id.hashCode(),
                    extraId
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            id,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun cancelReminder(id: String) {
        workManager.cancelUniqueWork(id)
    }
}
