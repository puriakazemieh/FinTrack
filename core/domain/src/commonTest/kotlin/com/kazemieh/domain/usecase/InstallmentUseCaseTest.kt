package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentFrequency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InstallmentUseCaseTest {

    @Test
    fun remindersKeepTheCanonicalChannelAndInstallmentIdAfterEveryChange() = runTest {
        val scheduled = mutableListOf<Pair<String, Long?>>()
        val scheduler = object : FakeNotificationManager() {
            override fun scheduleReminder(
                id: String,
                title: String,
                message: String,
                scheduledTime: kotlinx.datetime.LocalDateTime,
                channelId: String,
                extraId: Long?
            ) {
                scheduled.add(channelId to extraId)
            }
        }
        val repository = FakeInstallmentRepository()
        val installment = Installment(
            id = 1L,
            title = "Reminder",
            installmentAmount = 5_000L,
            totalAmount = 15_000L,
            startDate = 1_700_000_000_000L,
            totalInstallments = 3,
            paidInstallments = 0,
            nextDueDate = 1_700_000_000_000L,
            frequency = InstallmentFrequency.MONTHLY,
            categoryId = 1L,
            sourceId = 1L,
            postAsTransaction = false,
            reminderEnabled = true,
            isCompleted = false
        )
        AddInstallmentUseCase(repository, scheduler)(installment, emptyList(), emptyList(), "Title", "Message")
        UpdateInstallmentUseCase(repository, scheduler)(installment, emptyList(), emptyList(), "Title", "Message")
        MarkInstallmentAsPaidUseCase(repository, FakeTransactionRepository(), scheduler)(1L, "Payment", "Title", "Message")

        assertEquals(
            List<Pair<String, Long?>>(3) { com.kazemieh.domain.notification.NotificationManager.CHANNEL_INSTALLMENT to 1L },
            scheduled
        )
    }

    @Test
    fun `add, get, update, and delete installment`() = runTest {
        val repo = FakeInstallmentRepository()
        val notifManager = FakeNotificationManager()
        val addUseCase = AddInstallmentUseCase(repo, notifManager)
        val updateUseCase = UpdateInstallmentUseCase(repo, notifManager)
        val deleteUseCase = DeleteInstallmentUseCase(repo, notifManager)
        val getUseCase = GetInstallmentUseCase(repo)
        val observeUseCase = ObserveInstallmentsUseCase(repo)

        val installment = Installment(
            id = 1L,
            title = "Test Installment",
            installmentAmount = 5000L,
            totalAmount = 50000L,
            startDate = 1000000L,
            totalInstallments = 10,
            paidInstallments = 0,
            nextDueDate = 1000000L,
            frequency = InstallmentFrequency.MONTHLY,
            categoryId = 1L,
            sourceId = 1L,
            postAsTransaction = true,
            reminderEnabled = false,
            isCompleted = false
        )

        // 1. Add
        addUseCase(installment, emptyList(), emptyList(), "Title", "Message")
        var saved = getUseCase(1L)
        assertNotNull(saved)
        assertEquals("Test Installment", saved.installment.title)

        // 2. Observe
        val list = observeUseCase().first()
        assertEquals(1, list.size)

        // 3. Update
        val updatedInstallment = saved.installment.copy(title = "Updated Installment", paidInstallments = 1)
        updateUseCase(updatedInstallment, emptyList(), emptyList(), "Title", "Message")
        saved = getUseCase(1L)
        assertNotNull(saved)
        assertEquals("Updated Installment", saved.installment.title)
        assertEquals(1, saved.installment.paidInstallments)

        // 4. Delete
        deleteUseCase(1L)
        val deleted = getUseCase(1L)
        assertTrue(deleted == null)
    }
}
