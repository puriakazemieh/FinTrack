package com.kazemieh.domain.usecase

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.domain.notification.NotificationManager
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

class FixedExpenseSchedulingTest {
    @Test fun disabledExpenseIsSavedWithoutScheduling() = runTest {
        val repository = FakeFixedExpenseRepository()
        val scheduler = RecordingScheduler()
        AddFixedExpenseUseCase(repository, scheduler)(expense().copy(reminderEnabled = false), "Title", "Message")
        assertEquals(false, repository.getFixedExpenseById(7L)!!.reminderEnabled)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test fun inactiveExpenseIsSavedWithoutScheduling() = runTest {
        val repository = FakeFixedExpenseRepository()
        val scheduler = RecordingScheduler()
        AddFixedExpenseUseCase(repository, scheduler)(expense().copy(isActive = false), "Title", "Message")
        assertEquals(false, repository.getFixedExpenseById(7L)!!.isActive)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test fun explicitReminderTimeOverridesTheAccountingDueDate() = runTest {
        val scheduler = RecordingScheduler()
        val model = expense().copy(reminderTime = 1800003600000L)
        AddFixedExpenseUseCase(FakeFixedExpenseRepository(), scheduler)(model, "Title", "Message")
        assertEquals(Instant.fromEpochMilliseconds(model.reminderTime!!).toLocalDateTime(TimeZone.currentSystemDefault()), scheduler.scheduled.single().time)
    }

    @Test fun legacyReminderUsesTheAccountingDueDateWhenNoExplicitTimeIsSaved() = runTest {
        val scheduler = RecordingScheduler()
        val model = expense()
        AddFixedExpenseUseCase(FakeFixedExpenseRepository(), scheduler)(model, "Title", "Message")
        assertEquals(Instant.fromEpochMilliseconds(model.nextDueDate).toLocalDateTime(TimeZone.currentSystemDefault()), scheduler.scheduled.single().time)
    }

    @Test fun deletedExpenseDoesNotSchedule() = runTest {
        val scheduler = RecordingScheduler()
        AddFixedExpenseUseCase(FakeFixedExpenseRepository(), scheduler)(expense().copy(syncStatus = SyncStatus.DELETED), "Title", "Message")
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test fun updatingCancelsTheOldRequestBeforeSchedulingTheNewTime() = runTest {
        val repository = FakeFixedExpenseRepository()
        repository.insertFixedExpense(expense())
        val scheduler = RecordingScheduler()
        val changed = expense().copy(reminderTime = 1800003600000L)
        UpdateFixedExpenseUseCase(repository, scheduler)(changed)
        assertEquals(listOf("cancel:fixed_expense_7", "schedule:fixed_expense_7"), scheduler.events)
        val request = scheduler.scheduled.single()
        assertEquals(NotificationManager.CHANNEL_FIXED_EXPENSE, request.channel)
        assertEquals(7L, request.recordId)
        assertEquals(Instant.fromEpochMilliseconds(changed.reminderTime!!).toLocalDateTime(TimeZone.currentSystemDefault()), request.time)
        assertEquals(changed.title, request.title)
        assertEquals(changed, repository.getFixedExpenseById(7L))
    }

    @Test fun disablingDuringEditCancelsWithoutLosingTheSavedTime() = runTest {
        val repository = FakeFixedExpenseRepository()
        repository.insertFixedExpense(expense())
        val scheduler = RecordingScheduler()
        val changed = expense().copy(reminderEnabled = false, reminderTime = 1800003600000L)
        UpdateFixedExpenseUseCase(repository, scheduler)(changed)
        assertEquals(listOf("fixed_expense_7"), scheduler.cancelled)
        assertTrue(scheduler.scheduled.isEmpty())
        assertEquals(changed, repository.getFixedExpenseById(7L))
    }

    @Test fun deactivatingDuringEditCancelsWithoutScheduling() = runTest {
        val repository = FakeFixedExpenseRepository()
        repository.insertFixedExpense(expense())
        val scheduler = RecordingScheduler()
        UpdateFixedExpenseUseCase(repository, scheduler)(expense().copy(isActive = false))
        assertEquals(listOf("fixed_expense_7"), scheduler.cancelled)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test fun failedUpdateLeavesTheOldReminderUntouched() = runTest {
        val repository = object : FakeFixedExpenseRepository() {
            override suspend fun updateFixedExpense(expense: FixedExpense) { error("Injected persistence failure") }
        }
        repository.insertFixedExpense(expense())
        val scheduler = RecordingScheduler()
        assertTrue(runCatching { UpdateFixedExpenseUseCase(repository, scheduler)(expense().copy(reminderEnabled = false)) }.isFailure)
        assertTrue(scheduler.events.isEmpty())
        assertEquals(expense(), repository.getFixedExpenseById(7L))
    }

    @Test fun deleteRemovesTheRecordAndCancelsItsOwnRequest() = runTest {
        val repository = FakeFixedExpenseRepository()
        repository.insertFixedExpense(expense())
        val scheduler = RecordingScheduler()
        DeleteFixedExpenseUseCase(repository, scheduler)(7L)
        assertNull(repository.getFixedExpenseById(7L))
        assertEquals(listOf("fixed_expense_7"), scheduler.cancelled)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test fun blankNotificationCopyFallsBackToTheSavedTitle() = runTest {
        val scheduler = RecordingScheduler()
        AddFixedExpenseUseCase(FakeFixedExpenseRepository(), scheduler)(expense(), "", "")
        assertEquals(expense().title, scheduler.scheduled.single().title)
        assertEquals(expense().title, scheduler.scheduled.single().message)
    }

    private fun expense() = FixedExpense(
        id = 7L, title = "Saved expense", amount = 1000L, categoryId = 1L, sourceId = 1L,
        recurrence = RecurrenceType.MONTHLY, startDate = 1800000000000L, nextDueDate = 1800000000000L
    )

    private data class Scheduled(val id: String, val title: String, val message: String, val time: LocalDateTime, val channel: String, val recordId: Long?)
    private class RecordingScheduler : FakeNotificationManager() {
        val scheduled = mutableListOf<Scheduled>()
        val cancelled = mutableListOf<String>()
        val events = mutableListOf<String>()
        override fun scheduleReminder(id: String, title: String, message: String, scheduledTime: LocalDateTime, channelId: String, extraId: Long?) {
            events += "schedule:$id"
            scheduled += Scheduled(id, title, message, scheduledTime, channelId, extraId)
        }
        override fun cancelReminder(id: String) {
            events += "cancel:$id"
            cancelled += id
        }
    }
}
