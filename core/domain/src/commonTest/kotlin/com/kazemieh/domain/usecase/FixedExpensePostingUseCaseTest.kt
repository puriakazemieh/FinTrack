package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

class FixedExpensePostingUseCaseTest {
    @Test fun atomicPortReceivesFreshRelationsDescriptionAndStoredCurrency() = runTest {
        val f = Fixture()
        val current = f.expense.copy(currencyCode = "USD", description = "fresh description", updatedAt = 42,
            tagIds = listOf(2), personIds = listOf(3))
        f.seed(current)
        assertEquals(9L, f.post(current.copy(description = "old description", tagIds = emptyList(), personIds = emptyList())))
        val (request, transaction) = f.calls.single()
        assertEquals(current, request.expense)
        assertEquals("USD", transaction.currencyCode)
        assertEquals("fresh description", transaction.description)
        assertTrue(f.tx.balanceDeltasApplied.isEmpty(), "Ordinary non-atomic insertion was used")
    }

    @Test fun retryUsesStableOperationWithoutSeparateDomainAdvancement() = runTest {
        val f = Fixture(); f.seed()
        f.post(f.expense); f.now += 1000; f.post(f.expense)
        assertEquals(2, f.calls.size)
        assertEquals(1, f.calls.map { it.first.operationId }.distinct().size)
        assertEquals(f.expense, f.fixed.getFixedExpenseById(1))
        assertTrue(f.fixed.postings.isEmpty(), "Only storage may persist the receipt")
    }

    @Test fun reloadedRowDoesNotPrepayNextPeriodWithoutConfirmation() = runTest {
        val f = Fixture()
        val next = f.expense.copy(nextDueDate = f.now + 86400000)
        f.seed(next); f.primary(f.expense.nextDueDate)
        assertEquals(0L, f.post(next))
        assertEquals(0L, f.post(f.expense))
        assertTrue(f.calls.isEmpty())
    }

    @Test fun explicitOccurrenceSelectsNextDueForWorkerCatchUp() = runTest {
        val f = Fixture()
        val next = f.expense.copy(nextDueDate = f.expense.nextDueDate + 1000)
        f.seed(next); f.primary(f.expense.nextDueDate)
        assertEquals(9L, f.post(next, periodDueDate = next.nextDueDate))
        assertEquals(next.nextDueDate, f.calls.single().first.periodDueDate)
    }

    @Test fun confirmedRepeatKeepsPeriodAndCurrentSchedule() = runTest {
        val f = Fixture()
        val next = f.expense.copy(nextDueDate = f.now + 86400000)
        f.seed(next); f.primary(f.expense.nextDueDate)
        assertEquals(9L, f.post(next, repeatOperationId = "confirm-once"))
        val request = f.calls.single().first
        assertTrue(request.isRepeat)
        assertEquals("confirm-once", request.operationId)
        assertEquals(f.expense.nextDueDate, request.periodDueDate)
        assertEquals(next.nextDueDate, request.nextDueDate)
        assertEquals(next.isActive, request.nextIsActive)
    }

    @Test fun closedOneTimeRepeatNeedsExistingPrimaryAndNonblankConfirmation() = runTest {
        val f = Fixture(); val once = f.expense.copy(recurrence = RecurrenceType.ONCE, isActive = false)
        f.seed(once)
        assertEquals(0L, f.post(once, repeatOperationId = "confirmed"))
        f.primary(once.nextDueDate)
        assertEquals(0L, f.post(once, repeatOperationId = " "))
        assertEquals(9L, f.post(once, repeatOperationId = "confirmed"))
        assertFalse(f.calls.single().first.nextIsActive)
        assertEquals(once.nextDueDate, f.calls.single().first.nextDueDate)
    }

    @Test fun unsavedDeletedInactiveAndOutsideEndCannotPost() = runTest {
        val f = Fixture(); f.seed()
        assertEquals(0L, f.post(f.expense.copy(id = 0)))
        f.fixed.deleteFixedExpense(1)
        assertEquals(0L, f.post(f.expense))
        f.seed(f.expense.copy(isActive = false))
        assertEquals(0L, f.post(f.expense))
        f.seed(f.expense.copy(endDate = f.expense.nextDueDate - 1))
        assertEquals(0L, f.post(f.expense))
        assertTrue(f.calls.isEmpty())
    }

    @Test fun staleFinancialSelectionCannotSilentlyChangeTheCharge() = runTest {
        val f = Fixture(); f.seed()
        for (stale in listOf(f.expense.copy(amount = 200), f.expense.copy(currencyCode = "USD"),
            f.expense.copy(categoryId = 2), f.expense.copy(sourceId = 11))) assertEquals(0L, f.post(stale))
        assertTrue(f.calls.isEmpty())
    }

    @Test fun recurrenceAdvancesFromSelectedDueNotFromExecutionTime() = runTest {
        val f = Fixture()
        val due = Instant.parse("2026-11-25T08:00:00Z").toEpochMilliseconds()
        for ((recurrence, expected) in listOf(
            RecurrenceType.DAILY to "2026-11-26T08:00:00Z",
            RecurrenceType.WEEKLY to "2026-12-02T08:00:00Z",
            RecurrenceType.MONTHLY to "2026-12-25T08:00:00Z",
            RecurrenceType.YEARLY to "2027-11-25T08:00:00Z",
            RecurrenceType.CUSTOM to "2026-11-26T08:00:00Z"
        )) {
            f.calls.clear()
            val expense = f.expense.copy(recurrence = recurrence, startDate = due, nextDueDate = due)
            f.seed(expense); f.post(expense)
            assertEquals(Instant.parse(expected).toEpochMilliseconds(), f.calls.single().first.nextDueDate, recurrence.name)
        }
    }

    @Test fun monthEndClampsToLastValidPersianDay() = runTest {
        val f = Fixture()
        val due = Instant.parse("2026-09-22T08:00:00Z").toEpochMilliseconds() // 1405/06/31
        val expense = f.expense.copy(startDate = due, nextDueDate = due)
        f.seed(expense); f.post(expense)
        assertEquals(Instant.parse("2026-10-22T08:00:00Z").toEpochMilliseconds(), f.calls.single().first.nextDueDate)
    }

    @Test fun preservesFractionalSecondOfOriginalDue() = runTest {
        val f = Fixture()
        val due = Instant.parse("2026-11-25T08:00:00.321Z").toEpochMilliseconds()
        val expense = f.expense.copy(recurrence = RecurrenceType.DAILY, startDate = due, nextDueDate = due)
        f.seed(expense); f.post(expense)
        assertEquals(due + 86400000L, f.calls.single().first.nextDueDate)
    }

    @Test fun leapYearEndClampsToLastValidPersianDay() = runTest {
        val f = Fixture()
        val due = Instant.parse("2025-03-20T08:00:00Z").toEpochMilliseconds() // 1403/12/30
        val expense = f.expense.copy(recurrence = RecurrenceType.YEARLY, startDate = due, nextDueDate = due)
        f.seed(expense); f.post(expense)
        assertEquals(Instant.parse("2026-03-20T08:00:00Z").toEpochMilliseconds(), f.calls.single().first.nextDueDate)
    }

    @Test fun onceAndNoneCompleteWithoutAdvancing() = runTest {
        val f = Fixture()
        for (recurrence in listOf(RecurrenceType.ONCE, RecurrenceType.NONE)) {
            f.calls.clear(); val expense = f.expense.copy(recurrence = recurrence)
            f.seed(expense); f.post(expense)
            assertEquals(expense.nextDueDate, f.calls.single().first.nextDueDate)
            assertFalse(f.calls.single().first.nextIsActive)
        }
    }

    @Test fun endDateIsInclusiveAndStopsFurtherOccurrence() = runTest {
        val f = Fixture()
        val due = Instant.parse("2026-11-25T08:00:00Z").toEpochMilliseconds()
        val next = Instant.parse("2026-12-25T08:00:00Z").toEpochMilliseconds()
        for ((end, active) in listOf(next to true, (next - 1) to false)) {
            f.calls.clear(); val expense = f.expense.copy(startDate = due, nextDueDate = due, endDate = end)
            f.seed(expense); f.post(expense)
            assertEquals(active, f.calls.single().first.nextIsActive)
            assertEquals(if (active) next else due, f.calls.single().first.nextDueDate)
        }
    }

    @Test fun zeroOrExceptionFromAtomicStorageCannotAdvanceOutsideIt() = runTest {
        val f = Fixture(); f.seed(); f.result = 0
        assertEquals(0L, f.post(f.expense))
        assertEquals(f.expense, f.fixed.getFixedExpenseById(1))
        f.tx.fixedExpensePosting = { _, _ -> error("injected storage failure") }
        assertFailsWith<IllegalStateException> { f.post(f.expense) }
        assertEquals(f.expense, f.fixed.getFixedExpenseById(1))
    }

    @Test fun automaticPostingRechecksEnabledDueAndTypeAndDisallowsRepeat() = runTest {
        val f = Fixture(); f.seed()
        assertEquals(0L, f.post(f.expense, automatic = true))
        val future = f.expense.copy(nextDueDate = f.now + 1000, isAutoPostEnabled = true)
        f.seed(future)
        assertEquals(0L, f.post(future, automatic = true))
        val none = f.expense.copy(recurrence = RecurrenceType.NONE, isAutoPostEnabled = true)
        f.seed(none)
        assertEquals(0L, f.post(none, automatic = true))
        f.primary(none.nextDueDate)
        assertEquals(0L, f.post(none, repeatOperationId = "not-a-user-action", automatic = true))
        assertTrue(f.calls.isEmpty())
    }

    @Test fun dueAutomaticPostingUsesTheSameAtomicContract() = runTest {
        val f = Fixture(); val expense = f.expense.copy(isAutoPostEnabled = true)
        f.seed(expense)
        assertEquals(9L, f.post(expense, periodDueDate = expense.nextDueDate, automatic = true))
        assertEquals(expense.nextDueDate, f.calls.single().first.periodDueDate)
        assertFalse(f.calls.single().first.isRepeat)
    }

    @Test fun smsAndFixedExpenseCannotClaimTheSameTransaction() = runTest {
        val f = Fixture(); f.seed()
        val request = FixedExpensePostingRequest("owned", f.expense, f.expense.nextDueDate, f.now + 1000, true)
        assertFailsWith<IllegalArgumentException> {
            f.add(Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 10, type = TransactionType.EXPENSE),
                emptyList(), emptyList(), smsDraftId = 1, fixedExpensePosting = request)
        }
        assertTrue(f.calls.isEmpty())
    }

    @Test fun periodResolverIgnoresRepeatAndChangesOnlyWhenNextDueArrives() {
        val expense = FixedExpense(id = 1, title = "QA", amount = 100, categoryId = 1, sourceId = 10,
            recurrence = RecurrenceType.MONTHLY, startDate = 100, nextDueDate = 200)
        val receipts = listOf(FixedExpensePosting("a", 1, 100, 1, 100), FixedExpensePosting("b", 1, 100, 2, 110, true),
            FixedExpensePosting("other expense", 2, 150, 3, 150))
        assertEquals(100L, resolveFixedExpensePostingPeriod(expense, receipts, 199))
        assertEquals(200L, resolveFixedExpensePostingPeriod(expense, receipts, 200))
        assertEquals(200L, resolveFixedExpensePostingPeriod(expense, emptyList(), 199))
    }

    private class Fixture {
        val fixed = FakeFixedExpenseRepository()
        val tx = FakeTransactionRepository()
        val calls = mutableListOf<Pair<FixedExpensePostingRequest, Transaction>>()
        var result = 9L
        var now = Instant.parse("2026-11-25T10:00:00Z").toEpochMilliseconds()
        val expense = FixedExpense(id = 1, title = "QA", amount = 100, categoryId = 1, sourceId = 10,
            recurrence = RecurrenceType.MONTHLY, startDate = now - 60000, nextDueDate = now - 60000, reminderEnabled = false)
        private val achievements = FakeAchievementRepository()
        private val budgets = FakeBudgetRepository()
        val add = AddTransactionUseCase(tx, budgets, FakeNotificationManager(), UpdateStreakUseCase(achievements),
            CheckAchievementsUseCase(achievements, tx, budgets, UpdateXPUseCase(achievements)), UpdateXPUseCase(achievements))
        val post = PostFixedExpenseAsTransactionUseCase(fixed, add, GetDefaultCategoryUseCase(tx),
            GetDefaultFinancialSourceUseCase(tx), nowMillis = { now })
        init { tx.fixedExpensePosting = { request, transaction -> calls += request to transaction; result } }
        suspend fun seed(value: FixedExpense = expense) { fixed.insertFixedExpense(value) }
        fun primary(due: Long) { fixed.postings += FixedExpensePosting("primary-$due", 1, due, 1, now) }
    }
}
