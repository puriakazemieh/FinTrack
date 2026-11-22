package com.kazemieh.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.resolveNoteReminder
import com.kazemieh.domain.repository.NoteRepository
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.notif_note_private
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val notificationManager: NotificationManager by inject()
    private val noteRepository: NoteRepository by inject()

    override suspend fun doWork(): Result {
        var title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        var message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val savedChannelId = inputData.getString(KEY_CHANNEL_ID) ?: return Result.failure()
        val channelId = if (savedChannelId == "installment_reminders") {
            NotificationManager.CHANNEL_INSTALLMENT
        } else {
            savedChannelId
        }
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)
        var extraId = inputData.getLong(KEY_EXTRA_ID, -1L)

        if (channelId == NotificationManager.CHANNEL_NOTE) {
            val recordId = extraId.takeIf { inputData.keyValueMap.containsKey(KEY_EXTRA_ID) }
            val candidates = if (recordId == null) noteRepository.observeNotes().first()
                else listOfNotNull(noteRepository.getNoteById(recordId))
            val scheduled = if (inputData.keyValueMap.containsKey(KEY_SCHEDULED_TIME))
                inputData.getLong(KEY_SCHEDULED_TIME, -1) else null
            val note = resolveNoteReminder(candidates, recordId, notificationId, scheduled, System.currentTimeMillis())
                ?: return Result.success()
            extraId = note.id
            title = getString(Res.string.notes)
            message = if (note.isLocked) getString(Res.string.notif_note_private)
                else note.title.ifBlank { note.content }
        }

        notificationManager.showNotification(notificationId, title, message, channelId, extraId)

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_CHANNEL_ID = "channelId"
        const val KEY_NOTIFICATION_ID = "notificationId"
        const val KEY_EXTRA_ID = "extraId"
        const val KEY_SCHEDULED_TIME = "scheduledTimeMillis"

        fun createInputData(title: String, message: String, channelId: String, notificationId: Int, extraId: Long? = null, scheduledTimeMillis: Long? = null): Data {
            return Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .putString(KEY_CHANNEL_ID, channelId)
                .putInt(KEY_NOTIFICATION_ID, notificationId)
                .apply {
                    extraId?.let { putLong(KEY_EXTRA_ID, it) }
                    scheduledTimeMillis?.let { putLong(KEY_SCHEDULED_TIME, it) }
                }
                .build()
        }
    }
}
