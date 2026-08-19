package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DebtUseCaseTest {

    @Test
    fun `add, get, update, and delete debt`() = runTest {
        val repo = FakeDebtRepository()
        val notifManager = FakeNotificationManager()
        val addUseCase = AddDebtUseCase(repo, notifManager)
        val updateUseCase = UpdateDebtUseCase(repo, notifManager)
        val deleteUseCase = DeleteDebtUseCase(repo)
        val getUseCase = GetDebtByIdUseCase(repo)
        val observeUseCase = ObserveDebtsUseCase(repo)

        val debt = Debt(
            id = 1L,
            amount = 10000L,
            personId = 1L,
            type = DebtType.OWED_BY_ME,
            categoryId = 1L,
            sourceId = 1L,
            isSettled = false,
            date = 1000000L,
            reminderEnabled = false
        )

        // 1. Add
        addUseCase(debt, emptyList())
        var saved = getUseCase(1L)
        assertNotNull(saved)
        assertEquals(10000L, saved.amount)

        // 2. Observe
        val list = observeUseCase().first()
        assertEquals(1, list.size)

        // 3. Update
        val updatedDebt = saved.copy(amount = 15000L, isSettled = true)
        updateUseCase(updatedDebt, emptyList())
        saved = getUseCase(1L)
        assertNotNull(saved)
        assertEquals(15000L, saved.amount)
        assertTrue(saved.isSettled)

        // 4. Delete
        deleteUseCase(1L)
        val deleted = getUseCase(1L)
        assertTrue(deleted == null)
    }
}
