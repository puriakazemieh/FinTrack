package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class PostCommitFailureTest {
    @Test fun committedCreationIsNotReportedAsFailedWhenFollowUpEffectsFail() = runTest {
        val repository = FakeTransactionRepository()
        val budget = object : FakeBudgetRepository() {
            override suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress? =
                error("Injected budget follow-up failure")
        }
        val achievements = object : FakeAchievementRepository() {
            override suspend fun getStreak(): Streak = error("Injected achievement follow-up failure")
        }
        val add = AddTransactionUseCase(repository, budget, FakeNotificationManager(), UpdateStreakUseCase(achievements),
            CheckAchievementsUseCase(achievements, repository, budget, UpdateXPUseCase(achievements)), UpdateXPUseCase(achievements))
        assertEquals(1L, add(Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 1,
            type = TransactionType.EXPENSE), emptyList(), emptyList()))
        assertEquals(-100L, repository.balanceDeltasApplied[1])
    }
}
