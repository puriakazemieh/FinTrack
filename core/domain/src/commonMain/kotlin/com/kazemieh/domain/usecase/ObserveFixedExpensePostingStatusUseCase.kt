package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.FixedExpenseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock

/** A receipt still counts if its transaction was subsequently removed. */
data class FixedExpensePostingStatus(
    val periodDueDate: Long,
    val postingCount: Int,
    val hasPrimaryPosting: Boolean,
    val canRegister: Boolean
)

/** Shared by list and widget, including worker writes and time-only period changes. */
class ObserveFixedExpensePostingStatusUseCase(
    private val repository: FixedExpenseRepository,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    operator fun invoke(): Flow<Map<Long, FixedExpensePostingStatus>> = combine(
        repository.observeAllFixedExpenses(),
        repository.observeFixedExpensePostings(),
        flow {
            while (true) {
                emit(nowMillis())
                delay(30_000L)
            }
        }
    ) { expenses, postings, now ->
        val byExpense = postings.groupBy { it.expenseId }
        expenses.associate { expense ->
            val receipts = byExpense[expense.id].orEmpty()
            val period = resolveFixedExpensePostingPeriod(expense, receipts, now)
            val inPeriod = receipts.filter { it.periodDueDate == period }
            val hasPrimary = inPeriod.any { !it.isRepeat }
            expense.id to FixedExpensePostingStatus(
                periodDueDate = period,
                postingCount = inPeriod.size,
                hasPrimaryPosting = hasPrimary,
                canRegister = hasPrimary || (expense.isActive && period >= expense.startDate &&
                    expense.endDate?.let { period <= it } != false)
            )
        }
    }.distinctUntilChanged()
}
