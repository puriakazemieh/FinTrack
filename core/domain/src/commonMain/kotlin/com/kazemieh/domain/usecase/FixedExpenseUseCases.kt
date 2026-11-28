package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.fixedExpenseReminderTime
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private fun scheduleFixedExpenseReminder(
    expense: FixedExpense,
    scheduler: NotificationScheduler,
    title: String,
    message: String
) {
    val reminderTime = fixedExpenseReminderTime(expense) ?: return
    scheduler.scheduleReminder(
        id = "fixed_expense_${expense.id}",
        title = title.ifBlank { expense.title },
        message = message.ifBlank { expense.title },
        scheduledTime = Instant.fromEpochMilliseconds(reminderTime).toLocalDateTime(TimeZone.currentSystemDefault()),
        channelId = NotificationManager.CHANNEL_FIXED_EXPENSE,
        extraId = expense.id
    )
}

class AddFixedExpenseUseCase(
    private val repository: FixedExpenseRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(expense: FixedExpense, reminderTitle: String, reminderMessage: String) {
        val id = repository.insertFixedExpense(expense)
        scheduleFixedExpenseReminder(expense.copy(id = id), notificationScheduler, reminderTitle, reminderMessage)
    }
}

class UpdateFixedExpenseUseCase(
    private val repository: FixedExpenseRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        expense: FixedExpense,
        reminderTitle: String = expense.title,
        reminderMessage: String = expense.title
    ) {
        repository.updateFixedExpense(expense)
        notificationScheduler.cancelReminder("fixed_expense_${expense.id}")
        scheduleFixedExpenseReminder(expense, notificationScheduler, reminderTitle, reminderMessage)
    }
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
    private val getDefaultFinancialSourceUseCase: GetDefaultFinancialSourceUseCase,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    suspend operator fun invoke(
        expense: FixedExpense,
        periodDueDate: Long? = null,
        repeatOperationId: String? = null,
        automatic: Boolean = false
    ): Long {
        if (expense.id <= 0L) return 0L
        // Read the aggregate so persisted tag/person relations participate in the same claim.
        val current = fixedExpenseRepository.observeAllFixedExpenses().first().find { it.id == expense.id } ?: return 0L
        if (expense.amount != current.amount || expense.currencyCode != current.currencyCode ||
            expense.categoryId != current.categoryId || expense.sourceId != current.sourceId) return 0L
        val nowTs = nowMillis()
        val postings = fixedExpenseRepository.getFixedExpensePostings(current.id)
        val period = periodDueDate ?: resolveFixedExpensePostingPeriod(current, postings, nowTs)
        val alreadyPosted = postings.any { !it.isRepeat && it.periodDueDate == period }
        val repeat = repeatOperationId != null
        if (automatic && (!current.isAutoPostEnabled || current.recurrence == RecurrenceType.NONE ||
                repeat || period > nowTs)) return 0L
        if (repeat) {
            if (repeatOperationId.isNullOrBlank() || !alreadyPosted) return 0L
        } else {
            if (alreadyPosted || !current.isActive || period != current.nextDueDate ||
                period < current.startDate || current.endDate?.let { period > it } == true) return 0L
        }
        val timeZone = TimeZone.currentSystemDefault()
        val finalCategoryId = if (current.categoryId == 0L) {
            getDefaultCategoryUseCase(TransactionType.EXPENSE).id ?: 0L
        } else current.categoryId
        val finalSourceId = if (current.sourceId == 0L) {
            getDefaultFinancialSourceUseCase()?.id ?: 0L
        } else current.sourceId
        val (nextDue, nextIsActive) = if (repeat) current.nextDueDate to current.isActive else nextOccurrence(current)
        val request = FixedExpensePostingRequest(
            operationId = repeatOperationId ?: "fixed-expense:${current.id}:$period",
            expense = current, periodDueDate = period, nextDueDate = nextDue,
            nextIsActive = nextIsActive, isRepeat = repeat
        )
        val transaction = Transaction(
            id = 0,
            amount = current.amount,
            currencyCode = current.currencyCode,
            categoryId = finalCategoryId,
            sourceId = finalSourceId,
            description = current.description ?: current.title,
            timeStamp = nowTs,
            type = TransactionType.EXPENSE,
            date = Instant.fromEpochMilliseconds(nowTs).toPersianDateTime(timeZone).toDateString()
        )
        return addTransactionUseCase(
            transaction = transaction,
            tagIds = current.tagIds,
            personIds = current.personIds,
            fixedExpensePosting = request
        )
    }

    private fun nextOccurrence(expense: FixedExpense): Pair<Long, Boolean> {
        if (expense.recurrence == RecurrenceType.ONCE || expense.recurrence == RecurrenceType.NONE)
            return expense.nextDueDate to false
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

        // PersianDateTime retains whole seconds; keep the persisted subsecond precision.
        val millisecond = ((expense.nextDueDate % 1000L) + 1000L) % 1000L
        val nextTimestamp = next.toEpochMilliseconds(timeZone) + millisecond
        val endDate = expense.endDate
        return if (endDate != null && nextTimestamp > endDate) expense.nextDueDate to false else nextTimestamp to true
    }
}

/** Until the next due date arrives, the last posted occurrence is the UI's current period. */
fun resolveFixedExpensePostingPeriod(expense: FixedExpense, postings: List<FixedExpensePosting>, now: Long): Long =
    if (expense.nextDueDate > now) {
        postings.filter { it.expenseId == expense.id && !it.isRepeat && it.periodDueDate < expense.nextDueDate }
            .maxOfOrNull { it.periodDueDate } ?: expense.nextDueDate
    } else expense.nextDueDate

data class FixedExpenseUseCaseGroup(
    val addFixedExpenseUseCase: AddFixedExpenseUseCase,
    val updateFixedExpenseUseCase: UpdateFixedExpenseUseCase,
    val deleteFixedExpenseUseCase: DeleteFixedExpenseUseCase,
    val getFixedExpenseByIdUseCase: GetFixedExpenseByIdUseCase,
    val observeAllFixedExpensesUseCase: ObserveAllFixedExpensesUseCase,
    val observeFixedExpensesFilteredUseCase: ObserveFixedExpensesFilteredUseCase,
    val updateNextDueDateUseCase: UpdateNextDueDateUseCase,
    val postFixedExpenseAsTransactionUseCase: PostFixedExpenseAsTransactionUseCase,
    val observeFixedExpensePostingStatusUseCase: ObserveFixedExpensePostingStatusUseCase
)
