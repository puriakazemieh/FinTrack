package com.kazemieh.domain.notification

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.model.SyncStatus
import kotlin.test.*

class InstallmentReminderDeliveryTest {
    private val model = Installment(id = 42, title = "Current", totalAmount = 3000, installmentAmount = 1000,
        totalInstallments = 3, paidInstallments = 0, categoryId = 6, sourceId = 3,
        startDate = 1000, nextDueDate = 1000, frequency = InstallmentFrequency.MONTHLY)
    private fun resolve(records: List<Installment> = listOf(model), id: Long? = 42,
        hash: Int = "installment_42".hashCode(), scheduled: Long? = 1000, now: Long = 2000) =
        resolveInstallmentReminder(records, id, hash, scheduled, now)

    @Test fun currentDueRecordIsDelivered() { assertEquals(model, resolve()) }
    @Test fun exactDeadlineIsEligible() { assertEquals(model, resolve(now = 1000)) }
    @Test fun missingRecordIsSuppressed() { assertNull(resolve(emptyList())) }
    @Test fun deletedRecordIsSuppressed() { assertNull(resolve(listOf(model.copy(syncStatus = SyncStatus.DELETED)))) }
    @Test fun disabledReminderIsSuppressed() { assertNull(resolve(listOf(model.copy(reminderEnabled = false)))) }
    @Test fun completedRecordIsSuppressed() { assertNull(resolve(listOf(model.copy(isCompleted = true)))) }
    @Test fun fullyPaidRecordIsSuppressedEvenWithStaleCompletedFlag() {
        assertNull(resolve(listOf(model.copy(paidInstallments = 3))))
    }
    @Test fun invalidTotalCannotBecomeActionable() { assertNull(resolve(listOf(model.copy(totalInstallments = 0)))) }
    @Test fun postponedReminderIsSuppressed() { assertNull(resolve(listOf(model.copy(nextDueDate = 5000)))) }
    @Test fun oldJobCannotDeliverAnotherPastPeriod() { assertNull(resolve(listOf(model.copy(nextDueDate = 500)))) }
    @Test fun invalidExplicitIdNeverFallsBackToLegacyHash() { assertNull(resolve(id = 0)); assertNull(resolve(id = -1)) }
    @Test fun missingExplicitIdNeverFallsBackToOtherRecord() { assertNull(resolve(id = 7)) }
    @Test fun legacyJobResolvesUniqueRecord() { assertEquals(model, resolve(id = null, scheduled = null)) }
    @Test fun legacyHashCollisionIsNotGuessedEvenWhenOneRecordIsCompleted() {
        val a = model.copy(id = 5942400272365632431L)
        val b = model.copy(id = 1238714706934782301L, isCompleted = true)
        val hash = "installment_${a.id}".hashCode()
        assertEquals(hash, "installment_${b.id}".hashCode())
        assertNull(resolve(listOf(a, b), id = null, hash = hash, scheduled = null))
    }
    @Test fun fullLongIdDisambiguatesLegacyHashCollision() {
        val a = model.copy(id = 5942400272365632431L)
        val b = model.copy(id = 1238714706934782301L)
        assertEquals(b, resolve(listOf(a, b), id = b.id))
    }
    @Test fun editedTitleAmountCurrencyAreReadFromCurrentRecord() {
        val edited = model.copy(title = "Edited", installmentAmount = 7000, currencyCode = "USD")
        assertEquals(edited, resolve(listOf(edited)))
    }
}
