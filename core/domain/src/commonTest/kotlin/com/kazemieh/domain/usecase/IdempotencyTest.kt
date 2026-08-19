package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest

class IdempotencyTest {

    @Test
    fun `settle debt idempotency does not apply twice`() = runTest {
        val repo = FakeTransactionRepository()
        val debtRepo = FakeDebtRepository()
        
        val settleUseCase = SettleDebtUseCase(debtRepo, repo)
        
        val debtId = debtRepo.insertDebt(
            Debt(id = 1, amount = 5000L, personId = 1, type = DebtType.OWED_TO_ME, categoryId = 1, sourceId = 10, isSettled = false, date = 0L, reminderEnabled = false),
            emptyList()
        )
        
        settleUseCase(debtId, "Settle", postAsTransaction = true)
        
        val balancesAfterFirst = repo.balanceDeltasApplied.toMap()
        assertEquals(5000L, balancesAfterFirst[10])
        
        settleUseCase(debtId, "Settle again", postAsTransaction = true)
        
        assertEquals(balancesAfterFirst, repo.balanceDeltasApplied)
    }

    @Test
    fun `mark installment as paid idempotency for completed installment`() = runTest {
        val repo = FakeTransactionRepository()
        val instRepo = FakeInstallmentRepository()
        val notifManager = FakeNotificationManager()
        
        val markPaidUseCase = MarkInstallmentAsPaidUseCase(instRepo, repo, notifManager)
        
        val installment = Installment(
            id = 1, title = "Loan", installmentAmount = 1000L, totalAmount = 2000L, startDate = 0L,
            totalInstallments = 2, paidInstallments = 1,
            nextDueDate = 0L, frequency = InstallmentFrequency.MONTHLY,
            categoryId = 1, sourceId = 10, reminderEnabled = false,
            postAsTransaction = true, isCompleted = false
        )
        
        instRepo.insertInstallment(installment, emptyList(), emptyList())
        
        markPaidUseCase(1, "Paying", "Title", "Msg")
        val balancesAfterFirst = repo.balanceDeltasApplied.toMap()
        assertEquals(-1000L, balancesAfterFirst[10])
        
        val savedInst = instRepo.getInstallmentById(1)!!
        assertTrue(savedInst.isCompleted)
        assertEquals(2, savedInst.paidInstallments)
        
        markPaidUseCase(1, "Paying again", "Title", "Msg")
        
        assertEquals(balancesAfterFirst, repo.balanceDeltasApplied)
        val finalInst = instRepo.getInstallmentById(1)!!
        assertEquals(2, finalInst.paidInstallments) 
    }
    
    @Test
    fun `post fixed expense as transaction idempotency`() = runTest {
        val repo = FakeTransactionRepository()
        val fixedRepo = FakeFixedExpenseRepository()
        
        val postUseCase = PostFixedExpenseAsTransactionUseCase(
            fixedRepo,
            AddTransactionUseCase(repo, FakeBudgetRepository(), FakeNotificationManager(), UpdateStreakUseCase(FakeAchievementRepository()), CheckAchievementsUseCase(FakeAchievementRepository(), repo, FakeBudgetRepository(), UpdateXPUseCase(FakeAchievementRepository())), UpdateXPUseCase(FakeAchievementRepository())),
            GetDefaultCategoryUseCase(repo),
            GetDefaultFinancialSourceUseCase(repo)
        )
        
        val expense = FixedExpense(
            id = 1, title = "Netflix", amount = 500L, categoryId = 1, sourceId = 10,
            recurrence = RecurrenceType.ONCE, startDate = 0L, nextDueDate = 0L, isActive = true
        )
        
        fixedRepo.insertFixedExpense(expense)
        
        postUseCase(expense)
        
        val balancesAfterFirst = repo.balanceDeltasApplied.toMap()
        assertEquals(-500L, balancesAfterFirst[10])
        
        val savedExpense = fixedRepo.getFixedExpenseById(1)!!
        assertFalse(savedExpense.isActive)
        
        postUseCase(savedExpense)
        
        assertEquals(balancesAfterFirst, repo.balanceDeltasApplied)
    }
}
