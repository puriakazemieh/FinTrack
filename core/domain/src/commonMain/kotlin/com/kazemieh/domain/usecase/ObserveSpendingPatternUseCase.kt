package com.kazemieh.domain.usecase

import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

        return repository.observeCategorySums(currentFilter).map { currentSums ->
            val income = currentSums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
            val expense = currentSums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }
            
            val potentialAmount = (income - expense).coerceAtLeast(0)
            val potentialPercent = if (income > 0) (potentialAmount * 100 / income).toInt() else 0

            val topExpense = currentSums.filter { it.type == TransactionType.EXPENSE }.maxByOrNull { it.totalAmount }

            // Simple analysis for growth - in a real app we'd collect previous sums too
            // For now, let's provide a pattern based on current data
            
            SpendingPattern(
                totalIncome = income,
                totalExpense = expense,
                savingPotentialPercentage = potentialPercent,
                savingPotentialAmount = potentialAmount,
                topExpenseCategoryName = topExpense?.name,
                topExpenseAmount = topExpense?.totalAmount ?: 0,
                growthCategoryName = topExpense?.name, // Mocked growth for demo
                growthPercentage = 15
            )
        }
    }
}
