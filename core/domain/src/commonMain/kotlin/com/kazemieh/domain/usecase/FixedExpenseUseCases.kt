package com.kazemieh.domain.usecase

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.FixedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AddFixedExpenseUseCase(
    private val repository: FixedExpenseRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(expense: FixedExpense, reminderTitle: String, reminderMessage: String) {
        val id = repository.insertFixedExpense(expense)
        scheduleReminder(expense.copy(id = id), reminderTitle, reminderMessage)
    }

    private fun scheduleReminder(expense: FixedExpense, title: String, message: String) {
        val nextDueInstant = Instant.fromEpochMilliseconds(expense.nextDueDate)
        notificationScheduler.scheduleReminder(
            id = "fixed_expense_${expense.id}",
            title = title,
            message = message,
            scheduledTime = nextDueInstant.toLocalDateTime(TimeZone.currentSystemDefault()),
            channelId = NotificationManager.CHANNEL_INSTALLMENT // Use installment channel or add a new one
        )
    }
}

class UpdateFixedExpenseUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(expense: FixedExpense) = repository.updateFixedExpense(expense)
}

class DeleteFixedExpenseUseCase(
    private val repository: FixedExpenseRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteFixedExpense(id)
        notificationScheduler.cancelReminder("fixed_expense_$id")
    }
}

class GetFixedExpenseByIdUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(id: Long) = repository.getFixedExpenseById(id)
}

class ObserveAllFixedExpensesUseCase(private val repository: FixedExpenseRepository) {
    operator fun invoke(): Flow<List<FixedExpense>> = repository.observeAllFixedExpenses()
}

class UpdateNextDueDateUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(id: Long, nextDueDate: Long) = repository.updateNextDueDate(id, nextDueDate)
}

data class FixedExpenseUseCaseGroup(
    val addFixedExpenseUseCase: AddFixedExpenseUseCase,
    val updateFixedExpenseUseCase: UpdateFixedExpenseUseCase,
    val deleteFixedExpenseUseCase: DeleteFixedExpenseUseCase,
    val getFixedExpenseByIdUseCase: GetFixedExpenseByIdUseCase,
    val observeAllFixedExpensesUseCase: ObserveAllFixedExpensesUseCase,
    val updateNextDueDateUseCase: UpdateNextDueDateUseCase
)
