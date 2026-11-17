package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SmsPostingUseCaseTest {
    @Test fun smsUsesAtomicRepositoryAndRejectedAttemptHasNoFollowUpEffects() = runTest {
        var calledDraft: Long? = null
        val repository = object : FakeTransactionRepository() {
            override suspend fun addTransactionWithBalance(transaction: Transaction, tagIds: List<Long>,
                personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long = error("SMS must use atomic posting")
            override suspend fun addSmsTransactionWithBalance(smsDraftId: Long, transaction: Transaction,
                tagIds: List<Long>, personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long {
                calledDraft = smsDraftId
                assertEquals(mapOf(1L to -100L), balanceDeltas)
                return 0
            }
        }
        val budget = object : FakeBudgetRepository() {
            override suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress? =
                error("Consumed SMS must not emit a budget alert")
        }
        val achievements = object : FakeAchievementRepository() {
            override suspend fun getStreak(): Streak = error("Consumed SMS must not award again")
        }
        val add = AddTransactionUseCase(repository, budget, FakeNotificationManager(), UpdateStreakUseCase(achievements),
            CheckAchievementsUseCase(achievements, repository, budget, UpdateXPUseCase(achievements)), UpdateXPUseCase(achievements))
        assertEquals(0L, add(Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 1,
            type = TransactionType.EXPENSE), emptyList(), emptyList(), smsDraftId = 42))
        assertEquals(42L, calledDraft)
    }
}
