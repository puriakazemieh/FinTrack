package com.kazemieh.domain.usecase

import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.domain.PersianMonth
import com.kazemieh.domain.repository.TransactionRepository

/** One month's income vs. expense totals, for the report trend chart. */
data class MonthlyTrendPoint(
    val year: Int,
    val month: Int, // 1 = Farvardin … 12 = Esfand
    val label: String, // Persian month name, e.g. "تیر"
    val income: Long,
    val expense: Long
)

/**
 * Aggregates the user's transactions into the last [monthsBack] Persian months so the report can
 * plot an income-vs-expense trend. The axis always spans a continuous run of months ending at the
 * current month — months with no activity come back as zeros so the chart never has gaps.
 */
class GetMonthlyTrendUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(monthsBack: Int = 6): List<MonthlyTrendPoint> {
        val transactions = repository.getAllTransactions()
            .filter { it.syncStatus != SyncStatus.DELETED }

        // Bucket by an absolute month index (year * 12 + month-1) so months are comparable and
        // sortable across year boundaries.
        val incomeByKey = HashMap<Int, Long>()
        val expenseByKey = HashMap<Int, Long>()
        transactions.forEach { tx ->
            val pdt = PersianDateTime.parse(tx.timeStamp)
            val key = pdt.year * 12 + (pdt.month - 1)
            when (tx.type) {
                TransactionType.INCOME -> incomeByKey[key] = (incomeByKey[key] ?: 0L) + tx.amount
                TransactionType.EXPENSE -> expenseByKey[key] = (expenseByKey[key] ?: 0L) + tx.amount
                else -> Unit
            }
        }

        val now = PersianDateTime.parse(kotlin.time.Clock.System.now().toEpochMilliseconds())
        val currentKey = now.year * 12 + (now.month - 1)

        // Oldest first so the chart reads left-to-right through time.
        return (monthsBack - 1 downTo 0).map { offset ->
            val key = currentKey - offset
            val year = key / 12
            val month = key % 12 + 1
            MonthlyTrendPoint(
                year = year,
                month = month,
                label = PersianMonth.entries.getOrElse(month) { PersianMonth.UNKNOWN }.displayName,
                income = incomeByKey[key] ?: 0L,
                expense = expenseByKey[key] ?: 0L
            )
        }
    }
}
