package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.*
import java.nio.file.Files
import java.util.Properties
import kotlin.test.*

class CategoryDefaultAtomicityTest {
    private fun withDatabase(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { f.seed(); block(f) } finally { f.close() }
    }
    @Test fun rootsAndChildrenKeepOtherScopesAndCurrentMetadata() = withDatabase { f ->
        f.data.updateCategory(f.category(2).copy(name = "Renamed", description = "Latest description", colorId = 4))
        f.post()
        val before = f.snapshot()
        assertTrue(f.data.setDefaultCategory(2))
        assertEquals(listOf(2L), f.defaults(TransactionType.EXPENSE))
        assertEquals(2L, f.db.categoryQueries.getFirstByType(TransactionType.EXPENSE.count.toLong()).awaitAsOneOrNull()!!.id)
        assertEquals(listOf(3L), f.defaults(TransactionType.INCOME))
        assertEquals(listOf(5L), f.defaults(TransactionType.EXPENSE, 1))
        assertEquals(listOf(8L), f.defaults(TransactionType.EXPENSE, 4))
        assertEquals(listOf(10L), f.defaults(TransactionType.TRANSFER))
        val after = f.snapshot()
        for (i in before.indices) {
            val expected = if (before[i].id in listOf(1L, 2L)) before[i].copy(isDefault = before[i].id == 2L,
                updatedAt = after[i].updatedAt, syncStatus = after[i].syncStatus) else before[i]
            assertEquals(expected, after[i])
        }
        assertTrue(f.data.setDefaultCategory(6))
        assertEquals(listOf(6L), f.defaults(TransactionType.EXPENSE, 1))
        assertEquals(listOf(2L), f.defaults(TransactionType.EXPENSE))
        assertEquals(SyncStatus.DELETED, f.category(7).syncStatus)
        assertEquals(900L, f.data.getSourceById(1)!!.balance)
        assertEquals(1L, f.data.getTransactionCount())
    }
    @Test fun missingAndDeletedTargetsDoNotClearAnyDefault() = withDatabase { f ->
        val before = f.snapshot()
        assertFalse(f.data.setDefaultCategory(99999))
        assertFalse(f.data.setDefaultCategory(7))
        assertEquals(before, f.snapshot())
    }
    @Test fun scopeComesFromTheCurrentDatabaseRecord() = withDatabase { f ->
        f.data.updateCategory(f.category(2).copy(type = TransactionType.INCOME, parentId = 3, name = "Moved"))
        assertTrue(f.data.setDefaultCategory(2))
        assertEquals(listOf(2L), f.defaults(TransactionType.INCOME, 3))
        assertEquals(listOf(3L), f.defaults(TransactionType.INCOME))
        assertEquals(listOf(1L), f.defaults(TransactionType.EXPENSE))
        assertEquals(listOf(5L), f.defaults(TransactionType.EXPENSE, 1))
        assertEquals("Moved", f.category(2).name)
        assertEquals(3L, f.category(2).parentId)
    }
    @Test fun repeatSelectionNeverCreatesFinancialData() = withDatabase { f ->
        repeat(3) { assertTrue(f.data.setDefaultCategory(2)) }
        assertEquals(listOf(2L), f.defaults(TransactionType.EXPENSE))
        assertEquals(1000L, f.data.getSourceById(1)!!.balance)
        assertEquals(0L, f.data.getTransactionCount())
    }
    @Test fun longIdentityIsNotTruncated() = withDatabase { f ->
        val id = Int.MAX_VALUE.toLong() + 99
        f.data.insertFullCategory(Category(id = id, name = "Large ID", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
        assertTrue(f.data.setDefaultCategory(id))
        assertEquals(listOf(id), f.defaults(TransactionType.EXPENSE))
    }
    @Test fun failedUpdateRollsBackFlagsAndRetryWorks() = withDatabase { f ->
        val before = f.snapshot()
        f.driver.execute(null, "CREATE TRIGGER fail_category_default BEFORE UPDATE OF isDefault ON category " +
            "WHEN NEW.id = 2 BEGIN SELECT RAISE(ABORT, 'injected failure'); END", 0)
        assertTrue(runCatching { f.data.setDefaultCategory(2) }.isFailure)
        assertEquals(before, f.snapshot())
        f.driver.execute(null, "DROP TRIGGER fail_category_default", 0)
        assertTrue(f.data.setDefaultCategory(2))
        assertEquals(listOf(2L), f.defaults(TransactionType.EXPENSE))
    }
    @Test fun competingSelectionsAndPostingKeepOneDefaultPerScope() = withDatabase { f ->
        fun acceptableFailure(e: Throwable) = assertTrue(e.message?.contains("locked", true) == true ||
            e.message?.contains("SQLITE_BUSY") == true, "$e")
        val posting = coroutineScope {
            val choices = (1..8).map { n -> async { runCatching {
                assertTrue(f.data.setDefaultCategory(if (n % 2 == 0) 1 else 2))
                assertEquals(1, f.defaults(TransactionType.EXPENSE).size)
            } } }
            val write = async { runCatching { f.post() } }
            val results = choices.awaitAll()
            results.forEach { it.exceptionOrNull()?.let(::acceptableFailure) }
            assertTrue(results.any { it.isSuccess })
            write.await()
        }
        posting.exceptionOrNull()?.let { acceptableFailure(it); f.post() }
        assertTrue(f.data.setDefaultCategory(2))
        assertEquals(listOf(2L), f.defaults(TransactionType.EXPENSE))
        assertEquals(listOf(3L), f.defaults(TransactionType.INCOME))
        assertEquals(listOf(5L), f.defaults(TransactionType.EXPENSE, 1))
        assertEquals(900L, f.data.getSourceById(1)!!.balance)
        assertEquals(1L, f.data.getTransactionCount())
    }
    private class Fixture : AutoCloseable {
        private val dir = Files.createTempDirectory("fintrack-category-default-")
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
            data.insertFullSource(Source(id = 1, name = "QA", balance = 1000, colorId = 0, iconId = 0))
            for (id in 1L..10L) data.insertFullCategory(Category(id = id, name = "Category $id",
                type = when (id) { 3L, 9L -> TransactionType.INCOME; 10L -> TransactionType.TRANSFER; else -> TransactionType.EXPENSE },
                parentId = when (id) { 5L, 6L -> 1; 8L -> 4; 9L -> 3; else -> null },
                isDefault = id in listOf(1L, 3L, 5L, 7L, 8L, 9L, 10L),
                syncStatus = if (id == 7L) SyncStatus.DELETED else SyncStatus.SYNCED,
                colorId = 0, iconId = 0, updatedAt = id))
        }
        suspend fun category(id: Long) = data.getCategoryById(id)!!
        suspend fun snapshot() = (1L..10L).map { category(it) }
        suspend fun defaults(type: TransactionType, parent: Long? = null) =
            db.categoryQueries.observeCategories(type.count.toLong(), parent).awaitAsList()
                .filter { it.isDefault == 1L }.map { it.id }.sorted()
        suspend fun post() { data.addTransactionWithBalance(Transaction(id = 0, amount = 100, categoryId = 1,
            sourceId = 1, currencyCode = "IRT", type = TransactionType.EXPENSE), emptyList(), emptyList(), mapOf(1L to -100L)) }
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(dir.resolve("qa.db$suffix"))
            Files.deleteIfExists(dir)
        }
    }
}
