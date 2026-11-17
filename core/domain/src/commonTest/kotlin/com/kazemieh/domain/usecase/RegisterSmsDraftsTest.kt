package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.SmsDraftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class RegisterSmsDraftsTest {
    @Test fun threeReadyAndOneIncompletePostOnlyThreeAndKeepIncomplete() = runTest {
        val f = Fixture()
        val progress = mutableListOf<SmsBatchProgress>()
        val result = f.register(listOf(1, 2, 3, 4), "IRT") { progress += it }
        assertEquals(3, result.posted)
        assertEquals(1, result.incomplete)
        assertEquals(0, result.failed)
        assertEquals(listOf(0, 1, 2, 3, 4), progress.map { it.processed })
        assertEquals(0, progress.first().results.size) // Earlier progress snapshots remain immutable.
        assertEquals(listOf(100L, 200L, 300L), f.posted.values.map { it.amount })
        assertFalse(f.drafts.records.getValue(4).isUsed)
        assertEquals(-600L, f.repository.balanceDeltasApplied[1])
    }

    @Test fun failureAfterTwoPostsAndRetryDoNotDuplicateCompletedOnes() = runTest {
        val f = Fixture()
        f.failDraft = 3
        val first = f.register(listOf(1, 2, 3, 4), "IRT")
        assertEquals(2, first.posted)
        assertEquals(1, first.failed)
        assertEquals(1, first.incomplete)
        assertFalse(f.drafts.records.getValue(3).isUsed)
        f.failDraft = null
        val retry = f.register(listOf(1, 2, 3, 4), "IRT")
        assertEquals(1, retry.posted)
        assertEquals(2, retry.unavailable)
        assertEquals(1, retry.incomplete)
        assertEquals(3, f.posted.size)
        assertEquals(-600L, f.repository.balanceDeltasApplied[1])
    }

    @Test fun cancellationAfterSecondCommitCanResumeWithoutDoublePosting() = runTest {
        val f = Fixture()
        assertFailsWith<CancellationException> {
            f.register(listOf(1, 2, 3, 4), "IRT") { if (it.posted == 2) throw CancellationException("test interruption") }
        }
        assertEquals(2, f.posted.size)
        val retry = f.register(listOf(1, 2, 3, 4), "IRT")
        assertEquals(1, retry.posted)
        assertEquals(2, retry.unavailable)
        assertEquals(3, f.posted.size)
    }

    @Test fun duplicateAndMissingIdsAreCountedOnceAndDoNotWrite() = runTest {
        val f = Fixture()
        val result = f.register(listOf(1, 1, 999), "IRT")
        assertEquals(2, result.total)
        assertEquals(1, result.posted)
        assertEquals(1, result.unavailable)
        assertEquals(1, f.posted.size)
    }

    @Test fun wrongCurrencyAndLegacyAmountRequireReview() = runTest {
        val f = Fixture()
        assertEquals(SmsPreparationIssue.CURRENCY, f.register.registerOne(1, "USD").issue)
        f.drafts.records[1] = f.drafts.records.getValue(1).copy(body = "برداشت 1,000 تومان")
        assertEquals(SmsPreparationIssue.AMOUNT, f.register.registerOne(1, "IRT").issue)
        assertTrue(f.posted.isEmpty())
    }

    @Test fun invalidCategoryAndAmbiguousSourceAreNotReady() {
        val f = Fixture()
        val draft = f.drafts.records.getValue(1)
        assertEquals(SmsPreparationIssue.CATEGORY, (prepareSmsDraftTransaction(draft,
            listOf(f.category.copy(type = TransactionType.INCOME)), listOf(f.source), "IRT") as SmsDraftPreparation.NeedsInput).issue)
        assertEquals(SmsPreparationIssue.SOURCE, (prepareSmsDraftTransaction(draft.copy(sourceId = null),
            listOf(f.category), listOf(f.source, f.source.copy(id = 2)), "IRT") as SmsDraftPreparation.NeedsInput).issue)
        assertEquals(SmsPreparationIssue.TYPE, (prepareSmsDraftTransaction(draft.copy(type = TransactionType.ALL),
            listOf(f.category), listOf(f.source), "IRT") as SmsDraftPreparation.NeedsInput).issue)
    }

    private class Fixture {
        val category = Category(id = 1, name = "Food", type = TransactionType.EXPENSE, colorId = 0, iconId = 0)
        val source = Source(id = 1, name = "Blu", colorId = 0, iconId = 0)
        val drafts = Drafts()
        val posted = linkedMapOf<Long, Transaction>()
        var failDraft: Long? = null
        val repository = object : FakeTransactionRepository() {
            override fun observeSources(): Flow<List<Source>> = flowOf(listOf(source))
            override suspend fun getCategoryById(id: Long): Category? = category.takeIf { it.id == id }
            override suspend fun addSmsTransactionWithBalance(smsDraftId: Long, transaction: Transaction,
                tagIds: List<Long>, personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long {
                if (failDraft == smsDraftId) error("Injected write failure")
                if (drafts.records.getValue(smsDraftId).isUsed) return 0
                drafts.records[smsDraftId] = drafts.records.getValue(smsDraftId).copy(isUsed = true)
                posted[smsDraftId] = transaction
                balanceDeltas.forEach { (id, delta) -> balanceDeltasApplied[id] = (balanceDeltasApplied[id] ?: 0) + delta }
                return posted.size.toLong()
            }
        }
        private val achievements = FakeAchievementRepository()
        private val budget = FakeBudgetRepository()
        val register = RegisterSmsDraftsUseCase(drafts, repository,
            AddTransactionUseCase(repository, budget, FakeNotificationManager(), UpdateStreakUseCase(achievements),
                CheckAchievementsUseCase(achievements, repository, budget, UpdateXPUseCase(achievements)), UpdateXPUseCase(achievements)))
        init {
            for (id in 1L..4L) drafts.records[id] = SmsDraft(id = id, sender = "QA", body = "بانک تست برداشت ${id},000 ریال",
                amount = id * 1000, bankName = "Blu", type = TransactionType.EXPENSE,
                categoryId = if (id == 4L) null else 1, sourceId = 1, timeStamp = id, date = "1405/08/26")
        }
    }

    private class Drafts : SmsDraftRepository {
        val records = linkedMapOf<Long, SmsDraft>()
        override suspend fun addSmsDraft(smsDraft: SmsDraft): Long = error("not used")
        override fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>> = flowOf(records.values.filterNot { it.isUsed })
        override suspend fun markSmsDraftAsUsed(id: Long) { records[id] = records.getValue(id).copy(isUsed = true) }
        override suspend fun deleteSmsDraft(id: Long) { records.remove(id) }
        override suspend fun deleteUnusedSmsDrafts() = error("not used")
        override suspend fun deleteUnusedSmsDrafts(ids: List<Long>): List<Long> = error("not used")
        override suspend fun getSmsDraftById(id: Long): SmsDraft? = records[id]
        override suspend fun updateSmsDraft(smsDraft: SmsDraft) { records[smsDraft.id] = smsDraft }
    }
}
