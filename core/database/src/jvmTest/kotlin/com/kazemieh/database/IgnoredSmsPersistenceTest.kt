package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.datasource.SmsDraftLocalDataSourceImpl
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class IgnoredSmsPersistenceTest {
    private val lifetime = 30L * 24 * 60 * 60 * 1000
    private fun withDb(block: suspend (FinTrackDatabase, SmsDraftLocalDataSourceImpl, JdbcSqliteDriver) -> Unit) = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            FinTrackDatabase.Schema.synchronous().create(driver)
            val db = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            block(db, SmsDraftLocalDataSourceImpl(db), driver)
        } finally { driver.close() }
    }
    private suspend fun seed(sms: SmsDraftLocalDataSourceImpl, used: Boolean = false, ignored: Long? = null): Long =
        sms.insertSmsDraft(SmsDraft(sender = "QA", body = "bank", amount = 1000, bankName = "QA",
            type = TransactionType.EXPENSE, timeStamp = 1, date = "", isUsed = used, ignoredAt = ignored))

    @Test fun ignorePreservesDraftAndDoesNotConsumeIt() = withDb { db, sms, _ ->
        val id = seed(sms)
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 100, id = id)
        val saved = sms.getSmsDraftById(id)!!
        assertFalse(saved.isUsed)
        assertEquals(100L, saved.ignoredAt)
        assertEquals(1000L, saved.amount)
        assertEquals("bank", saved.body)
    }
    @Test fun repeatedIgnoreNeverExtendsDeadline() = withDb { db, sms, _ ->
        val id = seed(sms)
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 100, id = id)
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 500, id = id)
        assertEquals(100L, sms.getSmsDraftById(id)!!.ignoredAt)
    }
    @Test fun consumedAndMissingDraftsCannotBeIgnored() = withDb { db, sms, _ ->
        val id = seed(sms, used = true)
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 100, id = id)
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 100, id = 9999)
        assertNull(sms.getSmsDraftById(id)!!.ignoredAt)
        assertTrue(db.smsDraftQueries.observeUnusedSmsDrafts().awaitAsList().isEmpty())
    }
    @Test fun exactThirtyDayBoundaryOnlyDeletesUnusedIgnoredDrafts() = withDb { db, sms, _ ->
        val expired = seed(sms, ignored = 100)
        val fresh = seed(sms, ignored = 101)
        val ordinary = seed(sms)
        val consumed = seed(sms, used = true, ignored = 50)
        val now = 100 + lifetime
        assertEquals(listOf(fresh, expired), db.smsDraftQueries.observeIgnoredSmsDrafts(now - lifetime - 1).awaitAsList().map { it.id })
        assertEquals(listOf(fresh), db.smsDraftQueries.observeIgnoredSmsDrafts(now - lifetime).awaitAsList().map { it.id })
        db.smsDraftQueries.deleteExpiredIgnoredSmsDrafts(now - lifetime)
        assertNull(sms.getSmsDraftById(expired))
        assertNotNull(sms.getSmsDraftById(fresh))
        assertNotNull(sms.getSmsDraftById(ordinary))
        assertTrue(sms.getSmsDraftById(consumed)!!.isUsed)
        db.smsDraftQueries.deleteExpiredIgnoredSmsDrafts(now - lifetime)
        assertNotNull(sms.getSmsDraftById(fresh))
    }
    @Test fun staleEditCannotEraseIgnoredTimestamp() = withDb { db, sms, _ ->
        val id = seed(sms)
        val stale = sms.getSmsDraftById(id)!!
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 100, id = id)
        sms.updateSmsDraft(stale.copy(body = "edited"))
        assertEquals(100L, sms.getSmsDraftById(id)!!.ignoredAt)
        assertEquals("edited", sms.getSmsDraftById(id)!!.body)
    }
    @Test fun fullLongIdentityIsPreserved() = withDb { db, sms, driver ->
        val id = seed(sms)
        val large = Int.MAX_VALUE.toLong() + 99
        db.smsDraftQueries.ignoreUnusedSmsDraft(ignoredAt = 100, id = id)
        // A backup/import can carry a Long ID larger than an Android request code.
        driver.execute(null, "UPDATE sms_draft SET id = ? WHERE id = ?", 2) { bindLong(0, large); bindLong(1, id) }
        val restored = sms.getSmsDraftById(large)!!
        assertEquals(large, restored.id)
        assertEquals(100L, restored.ignoredAt)
    }
    @Test fun migrationLeavesLegacyUsedRowsAmbiguousAndUnchanged() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            driver.execute(null, "CREATE TABLE sms_draft (id INTEGER PRIMARY KEY, isUsed INTEGER NOT NULL)", 0)
            driver.execute(null, "INSERT INTO sms_draft VALUES (1,0),(2,1)", 0)
            FinTrackDatabase.Schema.synchronous().migrate(driver, 33, 34)
            driver.executeQuery(null, "SELECT count(*) FROM sms_draft WHERE ignoredAt IS NULL", { cursor ->
                cursor.next(); app.cash.sqldelight.db.QueryResult.Value(assertEquals(2L, cursor.getLong(0)))
            }, 0).value
        } finally { driver.close() }
    }
}
