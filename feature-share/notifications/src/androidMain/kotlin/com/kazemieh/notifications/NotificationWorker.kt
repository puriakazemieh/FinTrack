package com.kazemieh.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.kazemieh.domain.notification.NotificationManager

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val notificationManager: NotificationManager by inject()

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val channelId = inputData.getString(KEY_CHANNEL_ID) ?: return Result.failure()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)
        val extraId = inputData.getLong(KEY_EXTRA_ID, -1L)

        notificationManager.showNotification(notificationId, title, message, channelId, extraId)

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_CHANNEL_ID = "channelId"
        const val KEY_NOTIFICATION_ID = "notificationId"
        const val KEY_EXTRA_ID = "extraId"

        fun createInputData(title: String, message: String, channelId: String, notificationId: Int, extraId: Long? = null): Data {
            return Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .putString(KEY_CHANNEL_ID, channelId)
                .putInt(KEY_NOTIFICATION_ID, notificationId)
                .apply {
                    extraId?.let { putLong(KEY_EXTRA_ID, it) }
                }
                .build()
        }
    }
}
