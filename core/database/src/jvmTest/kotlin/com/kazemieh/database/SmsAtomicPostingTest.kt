package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.*
import com.kazemieh.database.datasource.SmsDraftLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.util.Properties
import kotlin.test.*

class SmsAtomicPostingTest {
    @Test fun retryConsumesExactlyOneDraftAndOneBalanceChange() = withDatabase { f ->
        assertTrue(f.post() > 0)
        assertEquals(0L, f.post())
        f.assertState(1, 900, true)
    }

    @Test fun lastStepFailureRollsBackTransactionRelationsBalanceAndDraftThenRetryWorks() = withDatabase { f ->
        val tag = 1L
        val person = 1L
        f.transactions.insertFullTag(Tag(id = tag, name = "QA", colorId = 0, iconId = 0))
        f.transactions.insertFullPerson(Person(id = person, name = "QA"))
        f.driver.execute(null, "CREATE TRIGGER fail_sms_use BEFORE UPDATE OF isUsed ON sms_draft " +
            "WHEN NEW.isUsed = 1 BEGIN SELECT RAISE(ABORT, 'injected SMS claim failure'); END", 0)
        assertTrue(runCatching { f.post(tags = listOf(tag), persons = listOf(person)) }.isFailure)
        f.assertState(0, 1000, false)
        assertTrue(f.database.transactionTagQueries.getTagsForTransaction(1).awaitAsList().isEmpty())
        assertTrue(f.database.transactionPersonQueries.getPersonsForTransaction(1).awaitAsList().isEmpty())
        f.driver.execute(null, "DROP TRIGGER fail_sms_use", 0)
        val id = f.post(tags = listOf(tag), persons = listOf(person))
        assertTrue(id > 0)
        assertEquals(1, f.database.transactionTagQueries.getTagsForTransaction(id).awaitAsList().size)
        assertEquals(1, f.database.transactionPersonQueries.getPersonsForTransaction(id).awaitAsList().size)
        assertEquals(0L, f.post())
        f.assertState(1, 900, true)
    }

    @Test fun concurrentAttemptsCannotDoublePost() = withDatabase { f ->
        val results = coroutineScope { (1..8).map { async { runCatching { f.post() } } }.awaitAll() }
        assertEquals(1, results.count { (it.getOrNull() ?: 0) > 0 })
        // A competing JDBC writer may report SQLITE_BUSY; a retry must remain harmless.
        assertEquals(0L, f.post())
        f.assertState(1, 900, true)
    }

    @Test fun staleCardEditCannotResurrectOrRewriteConsumedDraft() = withDatabase { f ->
        val stale = f.sms.getSmsDraftById(f.draftId)!!
        assertTrue(f.post() > 0)
        f.sms.updateSmsDraft(stale.copy(body = "stale UI", sourceId = null))
        val saved = f.sms.getSmsDraftById(f.draftId)!!
        assertTrue(saved.isUsed)
        assertEquals(stale.body, saved.body)
        assertEquals(stale.sourceId, saved.sourceId)
        assertEquals(0L, f.post())
    }

    @Test fun missingAndDeletedDraftCannotPost() = withDatabase { f ->
        assertEquals(0L, f.post(draftId = 999))
        f.sms.deleteSmsDraft(f.draftId)
        assertEquals(0L, f.post())
        assertEquals(0L, f.transactions.getTransactionCount())
        assertEquals(1000L, f.transactions.getSourceById(f.sourceId)!!.balance)
    }

    @Test fun currencyMismatchIsRejectedWithoutConsumingDraft() = withDatabase { f ->
        val source = f.transactions.getSourceById(f.sourceId)!!
        f.transactions.updateSource(source.copy(currencyCode = "USD"))
        assertEquals(0L, f.post())
        assertEquals(0L, f.post(transaction = f.transaction.copy(currencyCode = "USD")))
        f.assertState(0, 1000, false)
        f.transactions.updateSource(source)
        assertTrue(f.post() > 0)
    }

    @Test fun deletedSourceAndWrongCategoryAreRejected() = withDatabase { f ->
        f.driver.execute(null, "UPDATE source SET syncStatus = 2 WHERE id = ${f.sourceId}", 0)
        assertEquals(0L, f.post())
        f.driver.execute(null, "UPDATE source SET syncStatus = 0 WHERE id = ${f.sourceId}", 0)
        val income = 2L
        f.transactions.insertFullCategory(Category(id = income, name = "Income", type = TransactionType.INCOME, colorId = 0, iconId = 0))
        assertEquals(0L, f.post(transaction = f.transaction.copy(categoryId = income)))
        f.assertState(0, 1000, false)
    }

    @Test fun balanceOverflowIsRejectedAtomically() = withDatabase { f ->
        val source = f.transactions.getSourceById(f.sourceId)!!
        f.transactions.updateSource(source.copy(balance = Long.MIN_VALUE))
        assertEquals(0L, f.post())
        f.assertState(0, Long.MIN_VALUE, false)
    }

    @Test fun ordinaryTransactionDoesNotConsumeSms() = withDatabase { f ->
        assertTrue(f.transactions.addTransactionWithBalance(f.transaction, emptyList(), emptyList(), mapOf(f.sourceId to -100L)) > 0)
        f.assertState(1, 900, false)
    }

    @Test fun ignoredDraftCanPostOneMillisecondBeforeExpiry() = withDatabase { f ->
        f.database.smsDraftQueries.ignoreUnusedSmsDraft(f.now - SMS_IGNORED_RETENTION_MILLIS + 1, f.draftId)
        assertTrue(f.post() > 0)
        f.database.smsDraftQueries.deleteExpiredIgnoredSmsDrafts(f.now)
        f.assertState(1, 900, true)
    }

    @Test fun exactDeadlineRejectsPostEvenBeforeCleanupRuns() = withDatabase { f ->
        f.database.smsDraftQueries.ignoreUnusedSmsDraft(f.now - SMS_IGNORED_RETENTION_MILLIS, f.draftId)
        assertEquals(0L, f.post())
        f.assertState(0, 1000, false)
        assertEquals(0L, f.post())
        f.assertState(0, 1000, false)
    }

    @Test fun expiredIgnoredDraftIsRejectedAfterDeadline() = withDatabase { f ->
        f.database.smsDraftQueries.ignoreUnusedSmsDraft(f.now - SMS_IGNORED_RETENTION_MILLIS - 1, f.draftId)
        assertEquals(0L, f.post())
        f.assertState(0, 1000, false)
    }

    @Test fun staleCardAtDeadlineCannotWinRaceWithCleanup() = withDatabase { f ->
        f.database.smsDraftQueries.ignoreUnusedSmsDraft(f.now - SMS_IGNORED_RETENTION_MILLIS, f.draftId)
        val results = coroutineScope {
            val posting = async { runCatching { f.post() } }
            val cleanup = async { f.database.smsDraftQueries.deleteExpiredIgnoredSmsDrafts(f.now - SMS_IGNORED_RETENTION_MILLIS) }
            cleanup.await()
            listOf(posting.await())
        }
        assertTrue(results.none { (it.getOrNull() ?: 0L) > 0L })
        assertEquals(0L, f.post())
        assertEquals(0L, f.transactions.getTransactionCount())
        assertEquals(1000L, f.transactions.getSourceById(f.sourceId)!!.balance)
        assertNull(f.sms.getSmsDraftById(f.draftId))
    }

    @Test fun postingBeforeDeadlineSurvivesCleanupAfterDeadline() = withDatabase { f ->
        f.database.smsDraftQueries.ignoreUnusedSmsDraft(f.now - SMS_IGNORED_RETENTION_MILLIS + 1, f.draftId)
        val id = f.post()
        assertTrue(id > 0L)
        f.now += 1
        f.database.smsDraftQueries.deleteExpiredIgnoredSmsDrafts(f.now - SMS_IGNORED_RETENTION_MILLIS)
        f.assertState(1, 900, true)
        assertEquals(id, f.database.transactionQueries.getTransactionById(id).awaitAsList().single().id)
        assertEquals(0L, f.post())
    }

    private fun withDatabase(block: suspend (Fixture) -> Unit) = runBlocking {
        val fixture = Fixture()
        try { fixture.seed(); block(fixture) } finally { fixture.close() }
    }

    private class Fixture : AutoCloseable {
        private val directory = Files.createTempDirectory("fintrack-sms-atomic-")
        private val file = directory.resolve("qa.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:$file", Properties().apply {
            setProperty("foreign_keys", "true")
            setProperty("busy_timeout", "5000")
        })
        val database: FinTrackDatabase
        val transactions: TransactionLocalDataSourceImpl
        val sms: SmsDraftLocalDataSourceImpl
        var now = 4_000_000_000L
        var sourceId = 0L
        var draftId = 0L
        lateinit var transaction: Transaction
        init {
            FinTrackDatabase.Schema.synchronous().create(driver)
            database = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            transactions = TransactionLocalDataSourceImpl(database) { now }
            sms = SmsDraftLocalDataSourceImpl(database)
        }
        suspend fun seed() {
            sourceId = 1L
            transactions.insertFullSource(Source(id = sourceId, name = "QA", balance = 1000, colorId = 0, iconId = 0))
            val category = 1L
            transactions.insertFullCategory(Category(id = category, name = "Food", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
            transaction = Transaction(id = 0, amount = 100, categoryId = category, sourceId = sourceId, type = TransactionType.EXPENSE)
            sms.insertSmsDraft(SmsDraft(sender = "QA", body = "بانک تست برداشت 1,000 ریال", amount = 1000,
                bankName = "Unknown", type = TransactionType.EXPENSE, sourceId = sourceId, timeStamp = 1, date = "1405/08/26"))
            draftId = database.smsDraftQueries.observeUnusedSmsDrafts().awaitAsList().single().id
        }
        suspend fun post(draftId: Long = this.draftId, transaction: Transaction = this.transaction,
            tags: List<Long> = emptyList(), persons: List<Long> = emptyList()): Long =
            transactions.addSmsTransactionWithBalance(draftId, transaction, tags, persons, mapOf(transaction.sourceId to -transaction.amount))
        suspend fun assertState(count: Long, balance: Long, used: Boolean) {
            assertEquals(count, transactions.getTransactionCount())
            assertEquals(balance, transactions.getSourceById(sourceId)!!.balance)
            assertEquals(used, sms.getSmsDraftById(draftId)!!.isUsed)
        }
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(directory.resolve("qa.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }
}
