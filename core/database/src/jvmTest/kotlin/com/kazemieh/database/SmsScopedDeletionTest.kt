package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.*
import com.kazemieh.database.datasource.SmsDraftLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.util.Properties
import kotlin.test.*

class SmsScopedDeletionTest {
    @Test fun deletesOnlyConfirmedUnusedIdsAndPreservesNewArrivalAndPostedTransaction() = withDatabase { f ->
        val selected = f.ids()
        val transaction = Transaction(id = 0, amount = 100, sourceId = 1, categoryId = 1, type = TransactionType.EXPENSE)
        assertTrue(f.transactions.addSmsTransactionWithBalance(selected.first(), transaction, emptyList(), emptyList(), mapOf(1L to -100L)) > 0)
        f.insertDraft()
        val newId = f.ids().max()
        assertEquals(selected.drop(1), f.sms.deleteUnusedSmsDrafts(selected + selected.first()))
        assertTrue(f.sms.getSmsDraftById(selected.first())!!.isUsed)
        assertNotNull(f.sms.getSmsDraftById(newId))
        assertEquals(1L, f.transactions.getTransactionCount())
        assertEquals(900L, f.transactions.getSourceById(1)!!.balance)
        assertTrue(f.sms.deleteUnusedSmsDrafts(selected).isEmpty())
    }

    @Test fun failureDuringDeleteRollsBackAndCanRetry() = withDatabase { f ->
        val ids = f.ids()
        f.driver.execute(null, "CREATE TRIGGER fail_sms_delete BEFORE DELETE ON sms_draft WHEN OLD.id=${ids[1]} " +
            "BEGIN SELECT RAISE(ABORT,'injected delete failure'); END", 0)
        assertTrue(runCatching { f.sms.deleteUnusedSmsDrafts(ids) }.isFailure)
        assertEquals(ids, f.ids())
        f.driver.execute(null, "DROP TRIGGER fail_sms_delete", 0)
        assertEquals(ids, f.sms.deleteUnusedSmsDrafts(ids))
        assertTrue(f.ids().isEmpty())
    }

    private fun withDatabase(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { f.seed(); block(f) } finally { f.close() }
    }
    private class Fixture : AutoCloseable {
        private val file = Files.createTempFile("fintrack-sms-delete-", ".db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:$file", Properties().apply { setProperty("foreign_keys", "true") })
        private val database: FinTrackDatabase
        val sms: SmsDraftLocalDataSourceImpl
        val transactions: TransactionLocalDataSourceImpl
        init {
            FinTrackDatabase.Schema.synchronous().create(driver)
            database = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            sms = SmsDraftLocalDataSourceImpl(database)
            transactions = TransactionLocalDataSourceImpl(database)
        }
        suspend fun seed() {
            transactions.insertFullSource(Source(id = 1, name = "QA", balance = 1000, colorId = 0, iconId = 0))
            transactions.insertFullCategory(Category(id = 1, name = "Food", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
            repeat(3) { insertDraft() }
        }
        suspend fun insertDraft() {
            sms.insertSmsDraft(SmsDraft(sender = "QA", body = "بانک تست برداشت 1,000 ریال", amount = 1000,
                bankName = "Unknown", type = TransactionType.EXPENSE, timeStamp = 1, date = ""))
        }
        suspend fun ids(): List<Long> = database.smsDraftQueries.observeUnusedSmsDrafts().awaitAsList().map { it.id }.sorted()
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(file.resolveSibling(file.fileName.toString() + suffix))
        }
    }
}
