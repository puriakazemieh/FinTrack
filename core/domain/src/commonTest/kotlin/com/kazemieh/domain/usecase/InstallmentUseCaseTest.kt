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
