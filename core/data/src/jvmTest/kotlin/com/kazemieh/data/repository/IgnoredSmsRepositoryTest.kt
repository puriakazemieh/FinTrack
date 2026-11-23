package com.kazemieh.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.*
import com.kazemieh.database.*
import com.kazemieh.database.datasource.SmsDraftLocalDataSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.*

class IgnoredSmsRepositoryTest {
    private fun withDatabase(block: suspend (SmsDraftRepositoryImpl, SmsDraftLocalDataSourceImpl, AtomicLong) -> Unit) = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            FinTrackDatabase.Schema.synchronous().create(driver)
            val db = FinTrackDatabase(driver, com.kazemieh.database.Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            val local = SmsDraftLocalDataSourceImpl(db)
            val now = AtomicLong(4_000_000_000L)
            block(SmsDraftRepositoryImpl(local) { now.get() }, local, now)
        } finally { driver.close() }
    }
    private fun draft(ignoredAt: Long? = null, used: Boolean = false) = SmsDraft(sender = "QA", body = "bank", amount = 1000,
        bankName = "QA", type = TransactionType.EXPENSE, timeStamp = 1, date = "", ignoredAt = ignoredAt, isUsed = used)

    @Test fun ignoreRetainsFirstTimeAndFullIdentityWithoutConsumingDraft() = withDatabase { repo, local, now ->
        val id = repo.addSmsDraft(draft())
        repo.ignoreSmsDraft(id)
        val first = repo.getSmsDraftById(id)!!
        assertFalse(first.isUsed)
        assertEquals(now.get(), first.ignoredAt)
        now.addAndGet(1000)
        repo.ignoreSmsDraft(id)
        assertEquals(first, local.getSmsDraftById(id))
        assertEquals(listOf(id), repo.observeUnusedSmsDrafts().first().map { it.id })
    }
    @Test fun readingCleansExpiredOnlyAndPreservesOrdinaryAndConsumedRows() = withDatabase { repo, local, now ->
        val expired = repo.addSmsDraft(draft(now.get() - SMS_IGNORED_RETENTION_MILLIS))
        val fresh = repo.addSmsDraft(draft(now.get() - SMS_IGNORED_RETENTION_MILLIS + 1))
        val ordinary = repo.addSmsDraft(draft())
        val consumed = repo.addSmsDraft(draft(now.get() - SMS_IGNORED_RETENTION_MILLIS, used = true))
        assertEquals(setOf(fresh, ordinary), repo.observeUnusedSmsDrafts().first().map { it.id }.toSet())
        assertNull(local.getSmsDraftById(expired))
        assertNotNull(local.getSmsDraftById(consumed))
        repo.deleteExpiredIgnoredSmsDrafts()
        assertNotNull(local.getSmsDraftById(fresh))
        assertNotNull(local.getSmsDraftById(ordinary))
    }
    @Test fun expiredStaleRecordCannotBeOpenedBeforeWorker() = withDatabase { repo, local, now ->
        val id = repo.addSmsDraft(draft(now.get() - SMS_IGNORED_RETENTION_MILLIS))
        assertNull(repo.getSmsDraftById(id))
        assertNotNull(local.getSmsDraftById(id))
    }
    @Test fun openListExpiresWithoutAnotherDatabaseWrite() = withDatabase { repo, local, now ->
        val start = now.get()
        val id = repo.addSmsDraft(draft(start - SMS_IGNORED_RETENTION_MILLIS + 40))
        val ordinary = repo.addSmsDraft(draft())
        val result = withTimeout(2000) {
            repo.observeUnusedSmsDrafts().first { rows ->
                if (rows.any { it.id == id }) { now.set(start + 40); false } else true
            }
        }
        assertEquals(listOf(ordinary), result.map { it.id })
        assertNotNull(local.getSmsDraftById(id)) // logical expiry does not depend on physical cleanup
    }
    @Test fun repeatedCleanupIsIdempotentAndNeverDeletesRegisteredData() = withDatabase { repo, local, now ->
        val id = repo.addSmsDraft(draft(now.get() - SMS_IGNORED_RETENTION_MILLIS - 1))
        val used = repo.addSmsDraft(draft(now.get() - SMS_IGNORED_RETENTION_MILLIS - 1, used = true))
        repeat(2) { repo.deleteExpiredIgnoredSmsDrafts() }
        assertNull(local.getSmsDraftById(id))
        assertTrue(local.getSmsDraftById(used)!!.isUsed)
    }
    @Test fun consumedDraftCannotMoveIntoIgnoredList() = withDatabase { repo, local, _ ->
        val id = repo.addSmsDraft(draft(used = true))
        repo.ignoreSmsDraft(id)
        assertNull(local.getSmsDraftById(id)!!.ignoredAt)
        assertTrue(repo.observeUnusedSmsDrafts().first().isEmpty())
    }
}
