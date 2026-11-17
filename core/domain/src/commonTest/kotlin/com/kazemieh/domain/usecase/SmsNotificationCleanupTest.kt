package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SmsNotificationCleanupTest {
    @Test fun committedSmsCancelsOnlyItsOwnTypeAndFullRecordId() = runTest {
        val cancelled = mutableListOf<Triple<Int, String, Long?>>()
        val f = fixture(1) { id, channel, record -> cancelled += Triple(id, channel, record) }
        assertEquals(1L, f(transaction(), emptyList(), emptyList(), smsDraftId = 4_294_967_338))
        assertEquals(listOf<Triple<Int, String, Long?>>(Triple(42, NotificationManager.CHANNEL_SMS, 4_294_967_338L)), cancelled)
    }

    @Test fun rejectedOrRolledBackSmsDoesNotRemoveTheCard() = runTest {
        for (result in listOf(0L, -1L)) {
            val add = fixture(result) { _, _, _ -> error("Uncommitted SMS must keep its notification") }
            if (result == -1L) assertFailsWith<IllegalStateException> {
                add(transaction(), emptyList(), emptyList(), smsDraftId = 42)
            } else assertEquals(0L, add(transaction(), emptyList(), emptyList(), smsDraftId = 42))
        }
    }

    @Test fun notificationCleanupFailureDoesNotTurnACommittedWriteIntoFailure() = runTest {
        val add = fixture(1) { _, _, _ -> error("Injected notification cleanup failure") }
        assertEquals(1L, add(transaction(), emptyList(), emptyList(), smsDraftId = 42))
    }

    private fun transaction() = Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 1, type = TransactionType.EXPENSE)
    private fun fixture(result: Long, cancel: (Int, String, Long?) -> Unit): AddTransactionUseCase {
        val repository = object : FakeTransactionRepository() {
            override suspend fun addSmsTransactionWithBalance(smsDraftId: Long, transaction: Transaction,
                tagIds: List<Long>, personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long {
                if (result < 0) error("Injected atomic write failure")
                return result
            }
        }
        val notifications = object : NotificationManager by FakeNotificationManager() {
            override fun cancelNotification(id: Int, channelId: String, recordId: Long?) = cancel(id, channelId, recordId)
        }
        val budget = FakeBudgetRepository()
        val achievements = FakeAchievementRepository()
        return AddTransactionUseCase(repository, budget, notifications, UpdateStreakUseCase(achievements),
            CheckAchievementsUseCase(achievements, repository, budget, UpdateXPUseCase(achievements)), UpdateXPUseCase(achievements))
    }
}
