package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.*

class CategoryDefaultMigrationTest {
    private fun withLegacy(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { f.seed(); block(f) } finally { f.close() }
    }
    @Test fun preservesEffectiveDefaultAndEveryUnrelatedFieldAndTable() = withLegacy { f ->
        val before = f.snapshot()
        val rawBefore = f.rawCategories()
        f.migrate()
        val after = f.snapshot()
        assertEquals(before.filterKeys { it != "category" }, after.filterKeys { it != "category" })
        for (id in 1L..12L) {
            val old = f.beforeCategories.getValue(id)
            val actual = f.data.getCategoryById(id)!!
            if (id in listOf(1L, 3L, 7L)) {
                assertFalse(actual.isDefault)
                assertEquals(SyncStatus.MODIFIED, actual.syncStatus)
                assertEquals(old.copy(isDefault = false, updatedAt = actual.updatedAt, syncStatus = SyncStatus.MODIFIED), actual)
                assertTrue(actual.updatedAt >= old.updatedAt)
            } else assertEquals(old, actual)
        }
        val rawAfter = f.rawCategories()
        rawBefore.zip(rawAfter).forEach { (old, actual) ->
            val id = old.getValue("id")!!.toLong()
            val expected = if (id in listOf(1L, 3L, 7L)) old + mapOf("isDefault" to "0", "syncStatus" to "1", "updatedAt" to actual.getValue("updatedAt")) else old
            assertEquals(expected, actual)
        }
        assertEquals(listOf(2L, 4L, 8L, 9L, 12L), f.activeDefaults())
    }
    @Test fun applyingNormalizationAgainIsANoOpAndDeletedDefaultsStayDeleted() = withLegacy { f ->
        f.migrate()
        val first = f.snapshot()
        f.migrate()
        assertEquals(first, f.snapshot())
        assertEquals(f.beforeCategories.getValue(10), f.data.getCategoryById(10))
        assertEquals(listOf(2L, 4L, 8L, 9L, 12L), f.activeDefaults())
    }
    @Test fun statementFailureRollsBackAllFlagsBeforeRetry() = withLegacy { f ->
        val before = f.snapshot()
        f.driver.execute(null, "CREATE TRIGGER fail_normalize BEFORE UPDATE OF isDefault ON category " +
            "WHEN NEW.id=3 BEGIN SELECT RAISE(ABORT, 'migration failure'); END", 0)
        assertTrue(runCatching { f.migrate() }.isFailure)
        assertEquals(before, f.snapshot())
        f.driver.execute(null, "DROP TRIGGER fail_normalize", 0)
        f.migrate()
        assertEquals(listOf(2L, 4L, 8L, 9L, 12L), f.activeDefaults())
    }
    private class Fixture : AutoCloseable {
        private val dir = Files.createTempDirectory("fintrack-category-migration-")
        private val url = "jdbc:sqlite:" + dir.resolve("legacy.db")
        val driver = JdbcSqliteDriver(url)
        val db: FinTrackDatabase
        val data: TransactionLocalDataSourceImpl
        val beforeCategories = mutableMapOf<Long, Category>()
        init {
            // 34 and 35 have identical tables; migration 34 changes only duplicate flags.
            FinTrackDatabase.Schema.synchronous().create(driver)
            db = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            data = TransactionLocalDataSourceImpl(db)
            driver.execute(null, "PRAGMA user_version=34", 0)
        }
        suspend fun seed() {
            data.insertFullSource(Source(id=1,name="QA",balance=1000,colorId=1,iconId=1))
            for (id in 1L..12L) {
                val c = Category(id=id,name="Category $id",description="Keep $id",colorId=1,iconId=1,
                    type=when(id){1L,2L,3L,10L,12L->TransactionType.INCOME;11L->TransactionType.TRANSFER;else->TransactionType.EXPENSE},
                    parentId=when(id){7L,8L->4;9L->5;12L->2;else->null},
                    position=when(id){1L->5;2L,3L,8L->1;7L->2;10L->-1;else->0},
                    isDefault=id !in listOf(5L,6L,11L), updatedAt=100+id,
                    syncStatus=if(id==10L)SyncStatus.DELETED else SyncStatus.SYNCED)
                data.insertFullCategory(c)
                beforeCategories[id]=data.getCategoryById(id)!!
            }
            data.addTransactionWithBalance(Transaction(id=0,amount=100,categoryId=4,sourceId=1,
                currencyCode="IRT",type=TransactionType.EXPENSE),emptyList(),emptyList(),mapOf(1L to -100L))
        }
        // This fixture replays category normalization only; later migrations have separate fixtures.
        fun migrate() { FinTrackDatabase.Schema.synchronous().migrate(driver,34,35) }
        fun activeDefaults(): List<Long> = DriverManager.getConnection(url).use { c ->
            c.createStatement().use { s -> s.executeQuery("SELECT id FROM category WHERE syncStatus!=2 AND isDefault=1 ORDER BY id").use { r ->
                buildList { while(r.next()) add(r.getLong(1)) }
            } }
        }
        fun rawCategories(): List<Map<String,String?>> = DriverManager.getConnection(url).use { c ->
            c.createStatement().use { s -> s.executeQuery("SELECT * FROM category ORDER BY id").use { r ->
                val names = (1..r.metaData.columnCount).map { r.metaData.getColumnName(it) }
                buildList { while(r.next()) add(names.associateWith { r.getString(it) }) }
            } }
        }
        fun snapshot(): Map<String,List<List<String?>>> = DriverManager.getConnection(url).use { c ->
            val tables=c.createStatement().use { s -> s.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name").use { r ->
                buildList { while(r.next()) add(r.getString(1)) }
            } }
            tables.associateWith { table -> c.createStatement().use { s ->
                s.executeQuery("SELECT * FROM \""+table+"\" ORDER BY rowid").use { r ->
                    val count=r.metaData.columnCount
                    buildList { while(r.next()) add((1..count).map { r.getString(it) }) }
                }
            } }
        }
        override fun close() {
            driver.close()
            for(suffix in listOf("","-journal","-wal","-shm"))Files.deleteIfExists(dir.resolve("legacy.db$suffix"))
            Files.deleteIfExists(dir)
        }
    }
}
