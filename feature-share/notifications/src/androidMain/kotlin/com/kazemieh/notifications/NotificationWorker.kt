package com.kazemieh.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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

        notificationManager.showNotification(notificationId, title, message, channelId)

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_CHANNEL_ID = "channelId"
        const val KEY_NOTIFICATION_ID = "notificationId"

        fun createInputData(title: String, message: String, channelId: String, notificationId: Int): Data {
            return Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .putString(KEY_CHANNEL_ID, channelId)
                .putInt(KEY_NOTIFICATION_ID, notificationId)
                .build()
        }
    }
}
