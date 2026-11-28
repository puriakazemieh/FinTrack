package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FixedExpensePostingStatusTest {
    private val expense = FixedExpense(id = 1, title = "QA", amount = 100, categoryId = 1, sourceId = 1,
        recurrence = RecurrenceType.DAILY, startDate = 100, nextDueDate = 200, reminderEnabled = false)
    private val primary = FixedExpensePosting("primary", 1, 100, 1, 101)

    private class Repository(expense: FixedExpense) : FakeFixedExpenseRepository() {
        val expenses = MutableStateFlow(listOf(expense))
        val receipts = MutableStateFlow(emptyList<FixedExpensePosting>())
        override fun observeAllFixedExpenses(): Flow<List<FixedExpense>> = expenses
        override fun observeFixedExpensePostings(): Flow<List<FixedExpensePosting>> = receipts
    }

    @Test fun primaryAndRepeatsUpdateTwoIndependentObserversWithoutExpenseChange() = runTest {
        val repo = Repository(expense)
        val useCase = ObserveFixedExpensePostingStatusUseCase(repo) { 150 }
        val left = mutableListOf<Map<Long, FixedExpensePostingStatus>>()
        val right = mutableListOf<Map<Long, FixedExpensePostingStatus>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase().toList(left) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase().toList(right) }
        runCurrent()
        assertEquals(FixedExpensePostingStatus(200, 0, false, true), left.last().getValue(1))
        repo.receipts.value = listOf(primary)
        runCurrent()
        assertEquals(FixedExpensePostingStatus(100, 1, true, true), left.last().getValue(1))
        repo.receipts.value += primary.copy(operationId = "repeat", transactionId = 2, isRepeat = true)
        runCurrent()
        assertEquals(FixedExpensePostingStatus(100, 2, true, true), left.last().getValue(1))
        assertEquals(left, right)
        assertEquals(listOf(expense), repo.expenses.value)
    }

    @Test fun nextDueArrivesWhileOpenWithoutDatabaseChange() = runTest {
        val repo = Repository(expense)
        repo.receipts.value = listOf(primary)
        var now = 199L
        val emissions = mutableListOf<Map<Long, FixedExpensePostingStatus>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveFixedExpensePostingStatusUseCase(repo) { now }().toList(emissions)
        }
        runCurrent()
        assertTrue(emissions.last().getValue(1).hasPrimaryPosting)
        now = 200
        advanceTimeBy(30_000); runCurrent()
        assertEquals(FixedExpensePostingStatus(200, 0, false, true), emissions.last().getValue(1))
    }

    @Test fun closedOneTimeAllowsExplicitRepeatButUnpostedInactiveDoesNot() = runTest {
        val repo = Repository(expense.copy(recurrence = RecurrenceType.ONCE, nextDueDate = 100, isActive = false))
        val useCase = ObserveFixedExpensePostingStatusUseCase(repo) { 150 }
        assertEquals(FixedExpensePostingStatus(100, 0, false, false), useCase().first().getValue(1))
        repo.receipts.value = listOf(primary)
        assertEquals(FixedExpensePostingStatus(100, 1, true, true), useCase().first().getValue(1))
    }

    @Test fun receiptsOfOtherExpenseOrPeriodCannotMarkThisPeriodPosted() = runTest {
        val repo = Repository(expense)
        repo.receipts.value = listOf(primary, primary.copy(operationId = "other", expenseId = 2, periodDueDate = 200))
        assertEquals(FixedExpensePostingStatus(200, 0, false, true),
            ObserveFixedExpensePostingStatusUseCase(repo) { 200 }().first().getValue(1))
    }

    @Test fun unpostedOutsideRangeIsDisabledAndRemovedExpenseDisappears() = runTest {
        val repo = Repository(expense.copy(endDate = 199))
        val states = mutableListOf<Map<Long, FixedExpensePostingStatus>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveFixedExpensePostingStatusUseCase(repo) { 200 }().toList(states)
        }
        runCurrent()
        assertFalse(states.last().getValue(1).canRegister)
        repo.expenses.value = listOf(expense.copy(startDate = 201))
        runCurrent()
        assertFalse(states.last().getValue(1).canRegister)
        repo.expenses.value = emptyList()
        runCurrent()
        assertTrue(states.last().isEmpty())
    }

    @Test fun clockTicksDoNotEmitUnchangedStatusAndCancellationReleasesSources() = runTest {
        val repo = Repository(expense)
        val values = mutableListOf<Map<Long, FixedExpensePostingStatus>>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveFixedExpensePostingStatusUseCase(repo) { 200 }().toList(values)
        }
        runCurrent(); advanceTimeBy(90_001); runCurrent()
        assertEquals(1, values.size)
        assertEquals(1, repo.expenses.subscriptionCount.value)
        assertEquals(1, repo.receipts.subscriptionCount.value)
        job.cancel(); runCurrent()
        assertEquals(0, repo.expenses.subscriptionCount.value)
        assertEquals(0, repo.receipts.subscriptionCount.value)
    }
}
