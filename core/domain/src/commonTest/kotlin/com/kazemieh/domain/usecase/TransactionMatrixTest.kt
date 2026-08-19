package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class TransactionMatrixTest {

    private fun setupUseCases(): Triple<AddTransactionUseCase, UpdateTransactionUseCase, FakeTransactionRepository> {
        val repo = FakeTransactionRepository()
        val addUseCase = AddTransactionUseCase(
            repository = repo,
            budgetRepository = FakeBudgetRepository(),
            notificationManager = FakeNotificationManager(),
            updateStreak = UpdateStreakUseCase(FakeAchievementRepository()),
            checkAchievements = CheckAchievementsUseCase(
                achievementRepository = FakeAchievementRepository(),
                transactionRepository = repo,
                budgetRepository = FakeBudgetRepository(),
                updateXP = UpdateXPUseCase(FakeGamificationRepository())
            ),
            updateXP = UpdateXPUseCase(FakeGamificationRepository())
        )
        val updateUseCase = UpdateTransactionUseCase(repo)
        return Triple(addUseCase, updateUseCase, repo)
    }

    @Test
    fun `matrix add expense updates balance`() = runTest {
        val (add, _, repo) = setupUseCases()
        val tx = Transaction(id = 0, amount = 5000L, categoryId = 1, sourceId = 10, type = TransactionType.EXPENSE)
        
        add(tx, emptyList(), emptyList())
        assertEquals(-5000L, repo.balanceDeltasApplied[10])
    }

    @Test
    fun `matrix add income updates balance`() = runTest {
        val (add, _, repo) = setupUseCases()
        val tx = Transaction(id = 0, amount = 10000L, categoryId = 1, sourceId = 10, type = TransactionType.INCOME)
        
        add(tx, emptyList(), emptyList())
        assertEquals(10000L, repo.balanceDeltasApplied[10])
    }

    @Test
    fun `matrix add transfer updates both balances and includes fee`() = runTest {
        val (add, _, repo) = setupUseCases()
        val tx = Transaction(
            id = 0, amount = 1000L, amountTransfer = 500L, // 500 is fee
            categoryId = 1, sourceId = 10, sourceEndId = 11, type = TransactionType.TRANSFER
        )
        
        add(tx, emptyList(), emptyList())
        assertEquals(-1500L, repo.balanceDeltasApplied[10]) // -1000 transfer, -500 fee
        assertEquals(1000L, repo.balanceDeltasApplied[11]) // +1000 transfer received
    }

    @Test
    fun `matrix update expense amount recalculates balance delta correctly`() = runTest {
        val (add, update, repo) = setupUseCases()
        val oldTx = Transaction(id = 0, amount = 5000L, categoryId = 1, sourceId = 10, type = TransactionType.EXPENSE)
        val id = add(oldTx, emptyList(), emptyList())
        
        val newTx = oldTx.copy(id = id, amount = 8000L) // Increase expense by 3000
        update(oldTx, newTx, emptyList(), emptyList())
        
        // Initial was -5000. Update should apply delta of -3000. Total applied = -8000.
        assertEquals(-8000L, repo.balanceDeltasApplied[10])
    }

    @Test
    fun `matrix delete expense restores balance`() = runTest {
        val (add, _, repo) = setupUseCases()
        val delete = DeleteTransactionUseCase(repo)
        val tx = Transaction(id = 0, amount = 5000L, categoryId = 1, sourceId = 10, type = TransactionType.EXPENSE)
        val id = add(tx, emptyList(), emptyList())
        
        delete(tx.copy(id = id))
        // Applied -5000 on add, then +5000 on delete. Total applied should be 0.
        assertEquals(0L, repo.balanceDeltasApplied[10])
    }
}
