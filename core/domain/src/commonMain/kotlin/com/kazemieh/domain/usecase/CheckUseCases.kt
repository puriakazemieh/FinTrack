package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.CheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AddCheckUseCase(
    private val repository: CheckRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(check: Check, reminderTitle: String, reminderMessage: String) {
        val id = repository.insertCheck(check)
        if (check.reminderEnabled) {
            scheduleReminder(check.copy(id = id), reminderTitle, reminderMessage)
        }
    }

    private fun scheduleReminder(check: Check, title: String, message: String) {
        val dueInstant = Instant.fromEpochMilliseconds(check.dueDate)
        notificationScheduler.scheduleReminder(
            id = "check_${check.id}",
            title = title,
            message = message,
            scheduledTime = dueInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
            channelId = NotificationManager.CHANNEL_CHEQUE
        )
    }
}

class UpdateCheckUseCase(
    private val repository: CheckRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        check: Check,
        reminderTitle: String? = null,
        reminderMessage: String? = null
    ) {
        repository.updateCheck(check)
        if (check.reminderEnabled && reminderTitle != null && reminderMessage != null) {
            val dueInstant = Instant.fromEpochMilliseconds(check.dueDate)
            notificationScheduler.scheduleReminder(
                id = "check_${check.id}",
                title = reminderTitle,
                message = reminderMessage,
                scheduledTime = dueInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
                channelId = NotificationManager.CHANNEL_CHEQUE
            )
        } else if (!check.reminderEnabled) {
            notificationScheduler.cancelReminder("check_${check.id}")
        }
    }
}

class DeleteCheckUseCase(
    private val repository: CheckRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteCheck(id)
        notificationScheduler.cancelReminder("check_$id")
    }
}

class GetCheckByIdUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(id: Long) = repository.getCheckById(id)
}

class ObserveAllChecksUseCase(private val repository: CheckRepository) {
    operator fun invoke(): Flow<List<Check>> = repository.observeAllChecks()
}

class ObserveChecksByStatusUseCase(private val repository: CheckRepository) {
    operator fun invoke(status: CheckStatus): Flow<List<Check>> = repository.observeChecksByStatus(status)
}

data class CheckUseCaseGroup(
    val addCheckUseCase: AddCheckUseCase,
    val updateCheckUseCase: UpdateCheckUseCase,
    val deleteCheckUseCase: DeleteCheckUseCase,
    val getCheckByIdUseCase: GetCheckByIdUseCase,
    val observeAllChecksUseCase: ObserveAllChecksUseCase,
    val observeChecksByStatusUseCase: ObserveChecksByStatusUseCase
)
