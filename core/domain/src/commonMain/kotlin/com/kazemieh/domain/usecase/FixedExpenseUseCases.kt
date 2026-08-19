package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

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

class ObserveFixedExpensesFilteredUseCase(private val repository: FixedExpenseRepository) {
    operator fun invoke(
        query: String?,
        categoryIds: List<Long>,
        sourceIds: List<Long>,
        tagIds: List<Long>,
        personIds: List<Long>
    ): Flow<List<FixedExpense>> = repository.observeFixedExpensesFiltered(
        query, categoryIds, sourceIds, tagIds, personIds
    )
}

class UpdateNextDueDateUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(id: Long, nextDueDate: Long) = repository.updateNextDueDate(id, nextDueDate)
}

class PostFixedExpenseAsTransactionUseCase(
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getDefaultCategoryUseCase: GetDefaultCategoryUseCase,
    private val getDefaultFinancialSourceUseCase: GetDefaultFinancialSourceUseCase
) {
    suspend operator fun invoke(expense: FixedExpense) {
        if (!expense.isActive) return

        val timeZone = TimeZone.currentSystemDefault()
        val nowTs = Clock.System.now().toEpochMilliseconds()

        val finalCategoryId = if (expense.categoryId == 0L) {
            getDefaultCategoryUseCase(TransactionType.EXPENSE).id ?: 0L
        } else expense.categoryId

        val finalSourceId = if (expense.sourceId == 0L) {
            getDefaultFinancialSourceUseCase()?.id ?: 0L
        } else expense.sourceId

        val transaction = Transaction(
            id = 0,
            amount = expense.amount,
            categoryId = finalCategoryId,
            sourceId = finalSourceId,
            description = expense.description ?: expense.title,
            timeStamp = nowTs,
            type = TransactionType.EXPENSE,
            date = Instant.fromEpochMilliseconds(nowTs).toPersianDateTime(timeZone).toDateString()
        )
        addTransactionUseCase(
            transaction = transaction,
            tagIds = expense.tagIds,
            personIds = expense.personIds
        )

        if (expense.recurrence == RecurrenceType.ONCE) {
            fixedExpenseRepository.updateFixedExpense(expense.copy(isActive = false))
        } else {
            advanceNextDueDate(expense)
        }
    }

    private suspend fun advanceNextDueDate(expense: FixedExpense) {
        val timeZone = TimeZone.currentSystemDefault()
        val currentDueDate = Instant.fromEpochMilliseconds(expense.nextDueDate).toPersianDateTime(timeZone)

        val next = when (expense.recurrence) {
            RecurrenceType.DAILY -> currentDueDate.plus(1, DateTimeUnit.DAY)
            RecurrenceType.WEEKLY -> currentDueDate.plus(7, DateTimeUnit.DAY)
            RecurrenceType.MONTHLY -> currentDueDate.plus(1, DateTimeUnit.MONTH)
            RecurrenceType.YEARLY -> currentDueDate.plus(1, DateTimeUnit.YEAR)
            RecurrenceType.CUSTOM -> currentDueDate.plus(1, DateTimeUnit.DAY)
            else -> currentDueDate
        }

        val nextTimestamp = next.toEpochMilliseconds(timeZone)
        val endDate = expense.endDate
        if (endDate != null && nextTimestamp > endDate) {
            fixedExpenseRepository.updateFixedExpense(expense.copy(isActive = false))
        } else {
            fixedExpenseRepository.updateNextDueDate(expense.id, nextTimestamp)
        }
    }
}

data class FixedExpenseUseCaseGroup(
    val addFixedExpenseUseCase: AddFixedExpenseUseCase,
    val updateFixedExpenseUseCase: UpdateFixedExpenseUseCase,
    val deleteFixedExpenseUseCase: DeleteFixedExpenseUseCase,
    val getFixedExpenseByIdUseCase: GetFixedExpenseByIdUseCase,
    val observeAllFixedExpensesUseCase: ObserveAllFixedExpensesUseCase,
    val observeFixedExpensesFilteredUseCase: ObserveFixedExpensesFilteredUseCase,
    val updateNextDueDateUseCase: UpdateNextDueDateUseCase,
    val postFixedExpenseAsTransactionUseCase: PostFixedExpenseAsTransactionUseCase
)
