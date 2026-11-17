package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RejectedTransactionTest {
    @Test fun rejectedInsertDoesNotCheckBudgetOrAwardAchievements() = runTest {
        val repo = object : FakeTransactionRepository() {
            override suspend fun addTransactionWithBalance(transaction: Transaction, tagIds: List<Long>,
                personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long = 0
        }
        val budget = object : FakeBudgetRepository() {
            override suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress? =
                error("Rejected creation must not check budget")
        }
        val achievements = object : FakeAchievementRepository() {
            override suspend fun getStreak(): Streak = error("Rejected creation must not award achievements")
        }
        val add = AddTransactionUseCase(repo, budget, FakeNotificationManager(), UpdateStreakUseCase(achievements),
            CheckAchievementsUseCase(achievements, repo, budget, UpdateXPUseCase(achievements)), UpdateXPUseCase(achievements))
        assertEquals(0L, add(Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 1,
            type = TransactionType.EXPENSE), emptyList(), emptyList()))
    }
}
