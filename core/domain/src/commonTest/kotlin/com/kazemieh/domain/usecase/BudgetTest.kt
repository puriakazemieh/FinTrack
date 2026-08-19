package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetPeriod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BudgetTest {

    private val fakeBudgetRepository = FakeBudgetRepository()

    private val addBudgetUseCase = AddBudgetUseCase(fakeBudgetRepository)
    private val deleteBudgetUseCase = DeleteBudgetUseCase(fakeBudgetRepository)
    private val observeBudgetsWithProgressUseCase = ObserveBudgetsWithProgressUseCase(fakeBudgetRepository)
    private val hasAnyBudgetsUseCase = HasAnyBudgetsUseCase(fakeBudgetRepository)
    private val getBudgetByCategoryIdUseCase = GetBudgetByCategoryIdUseCase(fakeBudgetRepository)

    @Test
    fun `test add and observe daily budget`() = runTest {
        val budget = Budget(
            categoryId = 1,
            amount = 1000L,
            period = BudgetPeriod.DAILY,
            startAt = 100L
        )

        addBudgetUseCase(budget)

        val budgets = observeBudgetsWithProgressUseCase(0, 200L).first()
        assertEquals(1, budgets.size)
        assertEquals(BudgetPeriod.DAILY, budgets[0].budget.period)
        assertEquals(1000L, budgets[0].budget.amount)
    }

    @Test
    fun `test add budgets of different periods and filter by date range`() = runTest {
        // Daily
        addBudgetUseCase(Budget(categoryId = 1, amount = 100L, period = BudgetPeriod.DAILY, startAt = 100L))
        // Weekly
        addBudgetUseCase(Budget(categoryId = 2, amount = 700L, period = BudgetPeriod.WEEKLY, startAt = 500L))
        // Monthly
        addBudgetUseCase(Budget(categoryId = 3, amount = 3000L, period = BudgetPeriod.MONTHLY, startAt = 1500L))

        // Query range [0, 600] should return daily and weekly
        val range1 = observeBudgetsWithProgressUseCase(0, 600L).first()
        assertEquals(2, range1.size)
        assertTrue(range1.any { it.budget.period == BudgetPeriod.DAILY })
        assertTrue(range1.any { it.budget.period == BudgetPeriod.WEEKLY })
        assertFalse(range1.any { it.budget.period == BudgetPeriod.MONTHLY })

        // Query range [1000, 2000] should return only monthly
        val range2 = observeBudgetsWithProgressUseCase(1000, 2000L).first()
        assertEquals(1, range2.size)
        assertEquals(BudgetPeriod.MONTHLY, range2[0].budget.period)
    }

    @Test
    fun `test budget soft delete removes it from active queries`() = runTest {
        val id = addBudgetUseCase(
            Budget(
                categoryId = 4,
                amount = 5000L,
                period = BudgetPeriod.YEARLY,
                startAt = 10L
            )
        )

        // Verify it exists
        assertTrue(hasAnyBudgetsUseCase())
        var currentBudget = getBudgetByCategoryIdUseCase(4)
        assertTrue(currentBudget != null)

        // Soft delete
        deleteBudgetUseCase(id)

        // Verify it is excluded
        assertFalse(hasAnyBudgetsUseCase())
        currentBudget = getBudgetByCategoryIdUseCase(4)
        assertTrue(currentBudget == null)

        // Verify it is excluded from flows
        val flowResult = observeBudgetsWithProgressUseCase(0, 100L).first()
        assertTrue(flowResult.isEmpty())
    }
}
