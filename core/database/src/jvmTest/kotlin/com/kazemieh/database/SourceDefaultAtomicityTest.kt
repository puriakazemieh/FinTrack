package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.util.Properties
import kotlin.test.*

class SourceDefaultAtomicityTest {
    private fun withDatabase(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { f.seed(); block(f) } finally { f.close() }
    }
    @Test fun flagsDoNotRewriteLiveBalancesNamesOrBankMetadata() = withDatabase { f ->
        f.data.addTransactionWithBalance(Transaction(id = 0, amount = 300, categoryId = 1, sourceId = 1,
            currencyCode = "IRT", type = TransactionType.EXPENSE), emptyList(), emptyList(), mapOf(1L to -300L))
        f.data.updateSource(f.source(2).copy(name = "Edited elsewhere", balance = 1500, cardNumber = "22229999"))
        val before = (1L..3L).map { f.source(it) }
        assertTrue(f.data.setDefaultSource(2))
        val after = (1L..3L).map { f.source(it) }
        assertEquals(700L, after[0].balance)
        assertEquals(1500L, after[1].balance)
        for (i in 0..1) assertEquals(before[i].copy(isDefault = i == 1,
            updatedAt = after[i].updatedAt, syncStatus = after[i].syncStatus), after[i])
        assertEquals(before[2], after[2]) // deleted defaults must never be resurrected
        assertEquals(1L, f.data.getTransactionCount())
        assertEquals(listOf(2L), f.activeDefaults())
    }
    @Test fun missingOrDeletedTargetCannotClearTheCurrentDefault() = withDatabase { f ->
        val before = (1L..3L).map { f.source(it) }
        assertFalse(f.data.setDefaultSource(9999))
        assertFalse(f.data.setDefaultSource(3))
        assertEquals(before, (1L..3L).map { f.source(it) })
        assertEquals(listOf(1L), f.activeDefaults())
    }
    @Test fun repeatSelectionCannotPostMoneyOrChangeBalances() = withDatabase { f ->
        repeat(3) { assertTrue(f.data.setDefaultSource(2)) }
        assertEquals(listOf(2L), f.activeDefaults())
        assertEquals(1000L, f.source(1).balance)
        assertEquals(2000L, f.source(2).balance)
        assertEquals(0L, f.data.getTransactionCount())
    }
    @Test fun fullLongSourceIdentityIsPreserved() = withDatabase { f ->
        val id = Int.MAX_VALUE.toLong() + 99
        f.data.insertFullSource(Source(id = id, name = "Large ID", balance = -500, colorId = 0, iconId = 0))
        assertTrue(f.data.setDefaultSource(id))
        assertEquals(listOf(id), f.activeDefaults())
        assertEquals(-500L, f.source(id).balance)
    }
    @Test fun databaseFailureRollsBackBothDefaultFlagsAndRetryWorks() = withDatabase { f ->
        val before = (1L..3L).map { f.source(it) }
        f.driver.execute(null, "CREATE TRIGGER fail_default BEFORE UPDATE OF isDefault ON source " +
            "WHEN NEW.id = 2 BEGIN SELECT RAISE(ABORT, 'injected default failure'); END", 0)
        assertTrue(runCatching { f.data.setDefaultSource(2) }.isFailure)
        assertEquals(before, (1L..3L).map { f.source(it) })
        f.driver.execute(null, "DROP TRIGGER fail_default", 0)
        assertTrue(f.data.setDefaultSource(2))
        assertEquals(listOf(2L), f.activeDefaults())
    }
    @Test fun competingSelectionsAndLedgerWriteCannotLoseBalanceOrExposeTwoDefaults() = withDatabase { f ->
        val results = coroutineScope {
            val selectors = (1..8).map { n -> async { runCatching {
                assertTrue(f.data.setDefaultSource(if (n % 2 == 0) 1 else 2))
                assertEquals(1, f.activeDefaults().size)
            } } }
            val posting = async { runCatching {
                f.data.addTransactionWithBalance(Transaction(id = 0, amount = 100, categoryId = 1,
                    sourceId = 1, currencyCode = "IRT", type = TransactionType.EXPENSE),
                    emptyList(), emptyList(), mapOf(1L to -100L))
            } }
            val selectionResults = selectors.awaitAll()
            selectionResults.forEach { r -> r.exceptionOrNull()?.let { e ->
                assertTrue(e.message?.contains("locked", true) == true || e.message?.contains("SQLITE_BUSY") == true, "$e")
            } }
            check(selectionResults.any { it.isSuccess })
            posting.await()
        }
        if (results.isFailure) {
            val e = results.exceptionOrNull()!!
            assertTrue(e.message?.contains("locked", true) == true || e.message?.contains("SQLITE_BUSY") == true, "$e")
            f.data.addTransactionWithBalance(Transaction(id = 0, amount = 100, categoryId = 1,
                sourceId = 1, currencyCode = "IRT", type = TransactionType.EXPENSE),
                emptyList(), emptyList(), mapOf(1L to -100L))
        }
        assertTrue(f.data.setDefaultSource(2))
        assertEquals(listOf(2L), f.activeDefaults())
        assertEquals(900L, f.source(1).balance)
        assertEquals(2000L, f.source(2).balance)
        assertEquals(1L, f.data.getTransactionCount())
        assertEquals(SyncStatus.DELETED, f.source(3).syncStatus)
    }

    private class Fixture : AutoCloseable {
        private val dir = Files.createTempDirectory("fintrack-default-")
        val driver = JdbcSqliteDriver("jdbc:sqlite:" + dir.resolve("qa.db"), Properties().apply {
            setProperty("foreign_keys", "true"); setProperty("busy_timeout", "5000")
        })
        val db: FinTrackDatabase
        val data: TransactionLocalDataSourceImpl
        init {
            FinTrackDatabase.Schema.synchronous().create(driver)
            db = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            data = TransactionLocalDataSourceImpl(db)
        }
        suspend fun seed() {
            data.insertFullSource(Source(id = 1, name = "Former", balance = 1000, isDefault = true,
                colorId = 0, iconId = 0, updatedAt = 10, description = "Preserve former", cardNumber = "1111"))
            data.insertFullSource(Source(id = 2, name = "Target", balance = 2000,
                colorId = 1, iconId = 1, updatedAt = 20, description = "Preserve target", cardNumber = "2222"))
            data.insertFullSource(Source(id = 3, name = "Tombstone", balance = 3000, isDefault = true,
                syncStatus = SyncStatus.DELETED, colorId = 0, iconId = 0, updatedAt = 30))
            data.insertFullCategory(Category(id = 1, name = "QA", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
        }
        suspend fun source(id: Long) = data.getSourceById(id)!!
        suspend fun activeDefaults() = db.sourceQueries.observeSources().awaitAsList().filter { it.isDefault == 1L }.map { it.id }.sorted()
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(dir.resolve("qa.db$suffix"))
            Files.deleteIfExists(dir)
        }
    }
}
