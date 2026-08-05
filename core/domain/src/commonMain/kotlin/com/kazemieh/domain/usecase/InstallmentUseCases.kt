package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class InstallmentUseCaseGroup(
    val addInstallmentUseCase: AddInstallmentUseCase,
    val observeInstallmentsUseCase: ObserveInstallmentsUseCase,
    val markInstallmentAsPaidUseCase: MarkInstallmentAsPaidUseCase,
    val deleteInstallmentUseCase: DeleteInstallmentUseCase,
    val getInstallmentUseCase: GetInstallmentUseCase,
    val updateInstallmentUseCase: UpdateInstallmentUseCase
)

class GetInstallmentUseCase(private val repository: InstallmentRepository) {
    suspend operator fun invoke(id: Long): InstallmentWithRelations? = repository.getInstallmentWithRelations(id)
}

class UpdateInstallmentUseCase(
    private val repository: InstallmentRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        installment: Installment,
        tagIds: List<Long>,
        personIds: List<Long>,
        reminderTitle: String,
        reminderMessage: String
    ) {
        repository.updateInstallment(installment, tagIds, personIds)
        if (!installment.isCompleted && installment.reminderEnabled) {
            val nextDueInstant = Instant.fromEpochMilliseconds(installment.nextDueDate)
            notificationScheduler.scheduleReminder(
                id = "installment_${installment.id}",
                title = reminderTitle,
                message = reminderMessage,
                scheduledTime = nextDueInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
                channelId = "installment_reminders"
            )
        } else {
            notificationScheduler.cancelReminder("installment_${installment.id}")
        }
    }
}

class AddInstallmentUseCase(
    private val repository: InstallmentRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        installment: Installment,
        tagIds: List<Long>,
        personIds: List<Long>,
        reminderTitle: String,
        reminderMessage: String
    ) {
        val id = repository.insertInstallment(installment, tagIds, personIds)
        if (installment.reminderEnabled) {
            scheduleReminder(installment.copy(id = id), reminderTitle, reminderMessage)
        }
    }

    private fun scheduleReminder(
        installment: Installment,
        reminderTitle: String,
        reminderMessage: String
    ) {
        val nextDueInstant = Instant.fromEpochMilliseconds(installment.nextDueDate)
        notificationScheduler.scheduleReminder(
            id = "installment_${installment.id}",
            title = reminderTitle,
            message = reminderMessage,
            scheduledTime = nextDueInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
            channelId = "installment_reminders"
        )
    }
}

class ObserveInstallmentsUseCase(private val repository: InstallmentRepository) {
    operator fun invoke(): Flow<List<InstallmentWithRelations>> = repository.observeInstallments()
}

class DeleteInstallmentUseCase(
    private val repository: InstallmentRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteInstallment(id)
        notificationScheduler.cancelReminder("installment_$id")
    }
}

class MarkInstallmentAsPaidUseCase(
    private val installmentRepository: InstallmentRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        installmentId: Long,
        transactionDescription: String,
        reminderTitle: String,
        reminderMessage: String
    ) {
        val installmentWithRelations = installmentRepository.getInstallmentWithRelations(installmentId) ?: return
        val installment = installmentWithRelations.installment
        if (installment.isCompleted) return

        val paidInstallments = installment.paidInstallments + 1
        val isCompleted = paidInstallments >= installment.totalInstallments

        // 1. Create Transaction
        val transaction = Transaction(
            id = 0,
            amount = installment.installmentAmount.toInt(),
            categoryId = installment.categoryId,
            sourceId = installment.sourceId,
            description = transactionDescription,
            type = TransactionType.EXPENSE,
            timeStamp = Clock.System.now().toEpochMilliseconds()
        )
        val tagIds = installmentWithRelations.tags.mapNotNull { it.id }
        val personIds = installmentWithRelations.persons.mapNotNull { it.id }

        if (installment.postAsTransaction) {
            transactionRepository.addTransactionWithBalance(
                transaction = transaction,
                tagIds = tagIds,
                personIds = personIds,
                balanceDeltas = mapOf(installment.sourceId to -installment.installmentAmount.toInt())
            )
        }

        // 2. Calculate next due date
        val nextDueDate = if (!isCompleted) {
            calculateNextDueDate(installment.nextDueDate, installment.frequency)
        } else {
            installment.nextDueDate
        }

        val updatedInstallment = installment.copy(
            paidInstallments = paidInstallments,
            isCompleted = isCompleted,
            nextDueDate = nextDueDate
        )

        // 3. Update Installment
        installmentRepository.updateInstallment(updatedInstallment, tagIds, personIds)

        // 4. Update Reminder
        if (!isCompleted && updatedInstallment.reminderEnabled) {
            scheduleReminder(updatedInstallment, reminderTitle, reminderMessage)
        } else {
            notificationScheduler.cancelReminder("installment_${installment.id}")
        }
    }

    private fun scheduleReminder(
        installment: Installment,
        reminderTitle: String,
        reminderMessage: String
    ) {
        val nextDueInstant = Instant.fromEpochMilliseconds(installment.nextDueDate)
        notificationScheduler.scheduleReminder(
            id = "installment_${installment.id}",
            title = reminderTitle,
            message = reminderMessage,
            scheduledTime = nextDueInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
            channelId = "installment_reminders"
        )
    }

    private fun calculateNextDueDate(currentDueDate: Long, frequency: InstallmentFrequency): Long {
        val dateTime = PersianDateTime.parse(currentDueDate, TimeZone.currentSystemDefault())
        val next = when (frequency) {
            InstallmentFrequency.DAILY -> dateTime.plus(1, DateTimeUnit.DAY)
            InstallmentFrequency.WEEKLY -> dateTime.plus(7, DateTimeUnit.DAY)
            InstallmentFrequency.MONTHLY -> dateTime.plus(1, DateTimeUnit.MONTH)
            InstallmentFrequency.YEARLY -> dateTime.plus(1, DateTimeUnit.YEAR)
        }
        return next.toEpochMilliseconds(TimeZone.currentSystemDefault())
    }
}
