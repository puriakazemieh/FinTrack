package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CheckUseCaseTest {

    @Test
    fun `add, get, update, and delete check`() = runTest {
        val repo = FakeCheckRepository()
        val notifManager = FakeNotificationManager()
        val addUseCase = AddCheckUseCase(repo, notifManager)
        val updateUseCase = UpdateCheckUseCase(repo, notifManager)
        val deleteUseCase = DeleteCheckUseCase(repo, notifManager)
        val getUseCase = GetCheckByIdUseCase(repo)
        val observeUseCase = ObserveAllChecksUseCase(repo)

        val check = Check(
            id = 1L,
            amount = 25000L,
            date = 1000000L,
            dueDate = 1000000L,
            isIncoming = true,
            status = CheckStatus.PENDING,
            description = "Test Check",
            personId = 1L,
            categoryId = 1L,
            sourceId = 1L,
            tagIds = emptyList(),
            personName = "John Doe"
        )

        // 1. Add
        addUseCase(check, "Reminder Title", "Reminder Message")
        var saved = getUseCase(1L)
        assertNotNull(saved)
        assertEquals(25000L, saved.amount)
        assertEquals(CheckStatus.PENDING, saved.status)

        // 2. Observe
        val list = observeUseCase().first()
        assertEquals(1, list.size)

        // 3. Update Status
        val updatedCheck = saved.copy(status = CheckStatus.PASSED)
        updateUseCase(updatedCheck, "Reminder Title", "Reminder Message")
        saved = getUseCase(1L)
        assertNotNull(saved)
        assertEquals(CheckStatus.PASSED, saved.status)

        // 4. Delete
        deleteUseCase(1L)
        val deleted = getUseCase(1L)
        assertTrue(deleted == null)
    }
}
