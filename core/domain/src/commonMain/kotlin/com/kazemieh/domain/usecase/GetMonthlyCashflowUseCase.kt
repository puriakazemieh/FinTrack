package com.kazemieh.domain.usecase

import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.domain.PersianMonth
import com.kazemieh.common.persiandatetime.extensions.dayOfWeekIndex
import com.kazemieh.common.persiandatetime.extensions.monthLength
import com.kazemieh.domain.repository.TransactionRepository

/** Per-day net cashflow for a single Persian month, laid out for a calendar grid. */
data class MonthlyCashflow(
    val year: Int,
    val month: Int,
    val monthLabel: String,
    val daysInMonth: Int,
    /** Weekday of the 1st, 0 = Saturday … 6 = Friday, so the UI can pad the leading cells. */
    val firstWeekdayOffset: Int,
    /** day-of-month (1-based) → income minus expense for that day. */
    val netByDay: Map<Int, Long>
)

/**
 * Buckets the current Persian month's transactions into a per-day net (income − expense) so the
 * report can draw a cashflow calendar — green days earned more than they spent, red days the
 * opposite.
 */
class GetMonthlyCashflowUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): MonthlyCashflow {
        val now = PersianDateTime.parse(kotlin.time.Clock.System.now().toEpochMilliseconds())
        val year = now.year
        val month = now.month

        val firstDay = PersianDateTime(year, month, 1)
        val daysInMonth = firstDay.monthLength()
        val offset = firstDay.dayOfWeekIndex

        val netByDay = HashMap<Int, Long>()
        repository.getAllTransactions()
            .asSequence()
            .filter { it.syncStatus != SyncStatus.DELETED }
            .forEach { tx ->
                val pdt = PersianDateTime.parse(tx.timeStamp)
                if (pdt.year == year && pdt.month == month) {
                    val delta = when (tx.type) {
                        TransactionType.INCOME -> tx.amount.toLong()
                        TransactionType.EXPENSE -> -tx.amount.toLong()
                        else -> 0L
                    }
                    netByDay[pdt.day] = (netByDay[pdt.day] ?: 0L) + delta
                }
            }

        return MonthlyCashflow(
            year = year,
            month = month,
            monthLabel = PersianMonth.entries.getOrElse(month) { PersianMonth.UNKNOWN }.displayName,
            daysInMonth = daysInMonth,
            firstWeekdayOffset = offset,
            netByDay = netByDay
        )
    }
}
