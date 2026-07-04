package com.kazemieh.domain.usecase

import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock

data class SpendingPattern(
    val totalIncome: Long,
    val totalExpense: Long,
    val savingPotentialPercentage: Int,
    val savingPotentialAmount: Long,
    val topExpenseCategoryName: String?,
    val topExpenseAmount: Long,
    val growthCategoryName: String?,
    val growthPercentage: Int
)

class ObserveSpendingPatternUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<SpendingPattern> {
        val now = Clock.System.now().toEpochMilliseconds()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000)

        val currentFilter = TransactionFilterParams(fromTimestamp = thirtyDaysAgo, toTimestamp = now)
        val previousFilter = TransactionFilterParams(fromTimestamp = sixtyDaysAgo, toTimestamp = thirtyDaysAgo)

        return combine(
            repository.observeCategorySums(currentFilter),
            repository.observeCategorySums(previousFilter)
        ) { currentSums, previousSums ->
            val income = currentSums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
            val expense = currentSums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }

            val potentialAmount = (income - expense).coerceAtLeast(0)
            val potentialPercent = if (income > 0) (potentialAmount * 100 / income).toInt() else 0

            val topExpense = currentSums.filter { it.type == TransactionType.EXPENSE }.maxByOrNull { it.totalAmount }

            // Real month-over-month growth: compare each expense category's current spend
            // to its spend in the previous 30-day window and pick the fastest-growing one.
            val previousExpenseByCategory = previousSums
                .filter { it.type == TransactionType.EXPENSE }
                .associateBy({ it.categoryId }, { it.totalAmount })

            val fastestGrowing = currentSums
                .filter { it.type == TransactionType.EXPENSE }
                .mapNotNull { current ->
                    val previous = previousExpenseByCategory[current.categoryId] ?: 0L
                    if (previous <= 0L) return@mapNotNull null
                    val growth = ((current.totalAmount - previous) * 100 / previous).toInt()
                    if (growth > 0) current.name to growth else null
                }
                .maxByOrNull { it.second }

            SpendingPattern(
                totalIncome = income,
                totalExpense = expense,
                savingPotentialPercentage = potentialPercent,
                savingPotentialAmount = potentialAmount,
                topExpenseCategoryName = topExpense?.name,
                topExpenseAmount = topExpense?.totalAmount ?: 0,
                growthCategoryName = fastestGrowing?.first,
                growthPercentage = fastestGrowing?.second ?: 0
            )
        }
    }
}
