package com.kazemieh.domain.usecase

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.domain.notification.NotificationManager
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FixedExpenseReminderTest {
    @Test
    fun fixedExpenseReminderUsesItsOwnChannelAndRecordId() = runTest {
        var scheduled = false
        val scheduler = object : FakeNotificationManager() {
            override fun scheduleReminder(
                id: String,
                title: String,
                message: String,
                scheduledTime: LocalDateTime,
                channelId: String,
                extraId: Long?
            ) {
                scheduled = true
                assertEquals("fixed_expense_7", id)
                assertEquals(NotificationManager.CHANNEL_FIXED_EXPENSE, channelId)
                assertEquals(7L, extraId)
            }
        }
        val expense = FixedExpense(
            id = 7L,
            title = "Test expense",
            amount = 1_000L,
            categoryId = 1L,
            sourceId = 1L,
            recurrence = RecurrenceType.MONTHLY,
            startDate = 1_700_000_000_000L,
            nextDueDate = 1_700_000_000_000L
        )
        AddFixedExpenseUseCase(FakeFixedExpenseRepository(), scheduler)(expense, "Title", "Message")
        assertTrue(scheduled)
    }
}
