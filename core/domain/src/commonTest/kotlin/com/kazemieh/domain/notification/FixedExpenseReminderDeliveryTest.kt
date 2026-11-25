package com.kazemieh.domain.notification

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.SyncStatus
import kotlin.test.*

class FixedExpenseReminderDeliveryTest {
    private val expense = FixedExpense(id = 42L, title = "Current expense", amount = 7000L,
        categoryId = 6L, sourceId = 3L, recurrence = RecurrenceType.MONTHLY,
        startDate = 500L, nextDueDate = 3000L, reminderEnabled = true, reminderTime = 1000L)
    private fun resolve(records: List<FixedExpense> = listOf(expense), id: Long? = 42L,
        hash: Int = "fixed_expense_42".hashCode(), scheduled: Long? = 1000L, now: Long = 2000L) =
        resolveFixedExpenseReminder(records, id, hash, scheduled, now)

    @Test fun currentDueExpenseIsDelivered() { assertEquals(expense, resolve()) }
    @Test fun missingRecordIsNotDelivered() { assertNull(resolve(emptyList())) }
    @Test fun softDeletedRecordIsNotDelivered() { assertNull(resolve(listOf(expense.copy(syncStatus = SyncStatus.DELETED)))) }
    @Test fun inactiveRecordIsNotDelivered() { assertNull(resolve(listOf(expense.copy(isActive = false)))) }
    @Test fun disabledReminderIsNotDelivered() { assertNull(resolve(listOf(expense.copy(reminderEnabled = false)))) }
    @Test fun postponedReminderIsNotDeliveredEarly() { assertNull(resolve(listOf(expense.copy(reminderTime = 5000L)))) }
    @Test fun oldJobCannotDeliverADifferentPastOccurrence() { assertNull(resolve(listOf(expense.copy(reminderTime = 500L)))) }
    @Test fun exactDeadlineIsEligible() { assertEquals(expense, resolve(now = 1000L)) }
    @Test fun explicitIdNeverFallsBackToAnotherRecordsHash() { assertNull(resolve(id = 7L)) }
    @Test fun invalidExplicitIdsDoNotBecomeLegacyJobs() { assertNull(resolve(id = 0L)); assertNull(resolve(id = -1L)) }
    @Test fun oldRecordWithoutExplicitTimeUsesAccountingDueDate() {
        val old = expense.copy(reminderTime = null)
        assertEquals(old, resolve(listOf(old), scheduled = 3000L, now = 3000L))
        assertNull(resolve(listOf(old), scheduled = 1000L, now = 4000L))
    }
    @Test fun legacyJobResolvesOnlyAUniqueActiveHash() { assertEquals(expense, resolve(id = null, scheduled = null)) }
    @Test fun legacyHashCollisionIsNotGuessedEvenIfOneCandidateIsDisabled() {
        val first = expense.copy(id = 5_942_400_272_365_632_431L)
        val second = expense.copy(id = 1_238_714_706_934_782_301L, reminderEnabled = false)
        val hash = "fixed_expense_${first.id}".hashCode()
        assertEquals(hash, "fixed_expense_${second.id}".hashCode())
        assertNull(resolve(listOf(first, second), id = null, hash = hash, scheduled = null))
    }
    @Test fun fullLongIdDisambiguatesALegacyHashCollision() {
        val first = expense.copy(id = 5_942_400_272_365_632_431L)
        val second = expense.copy(id = 1_238_714_706_934_782_301L)
        assertEquals(second, resolve(listOf(first, second), id = second.id))
    }
    @Test fun editedTitleAmountAndCurrencyComeFromCurrentRecord() {
        val edited = expense.copy(title = "Edited expense", amount = 12345L, currencyCode = "USD")
        assertEquals(edited, resolve(listOf(edited)))
    }
}
