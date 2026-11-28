package com.kazemieh.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.*
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.datasource.*
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.domain.usecase.FixedExpensePostingStatus
import com.kazemieh.domain.usecase.ObserveFixedExpensePostingStatusUseCase
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.util.Properties
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.*

class FixedExpensePostingBackupTest {
    @Test fun receiptAndConfirmedRepeatSurviveJsonAndFreshRestore() = fixture { f ->
        val receipts = listOf(f.receipt, f.receipt.copy(operationId = "repeat", transactionId = 2, postedAt = 20, isRepeat = true))
        receipts.forEach { f.fixed.insertFullFixedExpensePosting(it) }
        val exported = f.backup.getBackupData(null, null)
        val decoded = Json.decodeFromString<BackupData>(Json.encodeToString(exported))
        assertEquals(receipts, decoded.fixedExpensePostings)
        val target = Fixture()
        try {
            target.backup.restoreBackupData(decoded)
            assertEquals(receipts, target.fixed.getAllFixedExpensePostings())
            assertEquals(exported.fixedExpenses, target.backup.getBackupData(null, null).fixedExpenses)
        } finally { target.close() }
    }

    @Test fun identicalReplayIsIdempotentAndConflictingReplayIsRejected() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        assertFailsWith<IllegalArgumentException> {
            f.fixed.insertFullFixedExpensePosting(f.receipt.copy(transactionId = 2))
        }
        assertEquals(listOf(f.receipt), f.fixed.getAllFixedExpensePostings())
    }

    @Test fun onePrimaryPerPeriodAllowsExplicitRepeatAndNextPeriod() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        assertFailsWith<Exception> { f.fixed.insertFullFixedExpensePosting(f.receipt.copy(operationId = "accidental-double")) }
        val repeat = f.receipt.copy(operationId = "confirmed", isRepeat = true, postedAt = 20)
        val next = f.receipt.copy(operationId = "next-period", periodDueDate = 200, postedAt = 30)
        f.fixed.insertFullFixedExpensePosting(repeat)
        f.fixed.insertFullFixedExpensePosting(next)
        assertEquals(listOf(next, repeat, f.receipt), f.fixed.getFixedExpensePostings(1))
    }

    @Test fun restoringExpenseInPlacePreservesHistoryAndExistingRelations() = fixture { f ->
        f.tx.insertFullTag(Tag(id = 1, name = "QA tag", colorId = 0, iconId = 0))
        f.tx.insertFullPerson(Person(id = 1, name = "QA person"))
        f.database.fixedExpenseTagQueries.insertFixedExpenseTag(1, 1)
        f.database.fixedExpensePersonQueries.insertFixedExpensePerson(1, 1)
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        f.fixed.insertFullFixedExpense(f.expense.copy(title = "Restored in place"))
        assertEquals(listOf(f.receipt), f.fixed.getAllFixedExpensePostings())
        val restored = f.fixed.getAllFixedExpenses().single()
        assertEquals("Restored in place", restored.title)
        assertEquals(listOf(1L), restored.tagIds)
        assertEquals(listOf(1L), restored.personIds)
    }

    @Test fun oldBackupWithoutNewFieldDecodesAndDoesNotEraseExistingReceipt() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        val data = f.backup.getBackupData(null, null)
        val legacy = JsonObject(Json.parseToJsonElement(Json.encodeToString(data)).jsonObject.filterKeys { it != "fixedExpensePostings" })
        val decoded = Json.decodeFromString<BackupData>(legacy.toString())
        assertTrue(decoded.fixedExpensePostings.isEmpty())
        f.backup.restoreBackupData(decoded)
        assertEquals(listOf(f.receipt), f.fixed.getAllFixedExpensePostings())
    }

    @Test fun conflictingReceiptRollsBackEarlierBackupWrites() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        val before = f.backup.getBackupData(null, null)
        val bad = before.copy(
            categories = before.categories + before.categories.single().copy(id = 2, name = "Must roll back"),
            fixedExpenses = listOf(f.expense.copy(title = "Must also roll back")),
            fixedExpensePostings = listOf(f.receipt.copy(transactionId = 2))
        )
        assertFailsWith<IllegalArgumentException> { f.backup.restoreBackupData(bad) }
        val after = f.backup.getBackupData(null, null)
        assertEquals(before.categories, after.categories)
        assertEquals(before.fixedExpenses, after.fixedExpenses)
        assertEquals(before.fixedExpensePostings, after.fixedExpensePostings)
    }

    @Test fun deltaUsesStrictReceiptTimestampAndFullDateFilterKeepsClaims() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        val repeat = f.receipt.copy(operationId = "delta", postedAt = 20, isRepeat = true)
        f.fixed.insertFullFixedExpensePosting(repeat)
        assertEquals(listOf(repeat), f.backup.getDeltaBackupData(10).fixedExpensePostings)
        assertTrue(f.backup.getDeltaBackupData(20).fixedExpensePostings.isEmpty())
        val filtered = f.backup.getBackupData(1000, 2000)
        assertTrue(filtered.transactions.isEmpty())
        assertEquals(listOf(f.receipt, repeat), filtered.fixedExpensePostings)
        val target = Fixture()
        try {
            target.backup.restoreBackupData(filtered)
            assertEquals(filtered.fixedExpensePostings, target.fixed.getAllFixedExpensePostings())
        } finally { target.close() }
    }

    @Test fun deletingTransactionOrExpenseKeepsReceiptAndOtherTransactions() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        f.tx.physicallyDeleteTransaction(1)
        assertEquals(listOf(f.receipt), f.fixed.getAllFixedExpensePostings())
        f.tx.insertFullTransaction(f.transaction.copy(id = 2))
        f.fixed.physicallyDeleteFixedExpense(1)
        assertEquals(listOf(f.receipt), f.fixed.getAllFixedExpensePostings())
        assertEquals(listOf(2L), f.tx.getAllTransactions().map { it.id })
        assertEquals(1000L, f.tx.getSourceById(1)!!.balance)
    }

    @Test fun deletedExpenseHistoryIsNotExportedWithoutItsParent() = fixture { f ->
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        f.fixed.deleteFixedExpense(1)
        assertTrue(f.backup.getBackupData(null, null).fixedExpenses.isEmpty())
        assertTrue(f.backup.getBackupData(null, null).fixedExpensePostings.isEmpty())
        assertTrue(f.backup.getDeltaBackupData(0).fixedExpensePostings.isEmpty())
    }

    @Test fun invalidOrParentlessReceiptIsRejectedWithoutAnyWrite() = fixture { f ->
        for (invalid in listOf(f.receipt.copy(operationId = " "), f.receipt.copy(expenseId = 0), f.receipt.copy(transactionId = 0))) {
            assertFailsWith<IllegalArgumentException> { f.fixed.insertFullFixedExpensePosting(invalid) }
        }
        assertFailsWith<Exception> { f.fixed.insertFullFixedExpensePosting(f.receipt.copy(expenseId = 999)) }
        assertTrue(f.fixed.getAllFixedExpensePostings().isEmpty())
    }

    @Test fun migration36To37CreatesEmptyLedgerAndPreservesExistingExpense() = fixture { f ->
        f.driver.execute(null, "DROP TABLE fixed_expense_posting", 0)
        FinTrackDatabase.Schema.synchronous().migrate(f.driver, 36, 37)
        assertEquals(f.expense, f.fixed.getFixedExpenseById(1))
        assertTrue(f.fixed.getAllFixedExpensePostings().isEmpty())
        f.fixed.insertFullFixedExpensePosting(f.receipt)
        assertEquals(listOf(f.receipt), f.fixed.getAllFixedExpensePostings())
    }

    @Test fun receiptFlowEmitsAfterInsert() = fixture { f ->
        coroutineScope {
            val updates = Channel<List<FixedExpensePosting>>(Channel.UNLIMITED)
            val job = launch { f.fixed.observeFixedExpensePostings().collect { updates.send(it) } }
            try {
                assertTrue(withTimeout(5000) { updates.receive() }.isEmpty())
                f.fixed.insertFullFixedExpensePosting(f.receipt)
                assertEquals(listOf(f.receipt), withTimeout(5000) { updates.receive() })
            } finally { job.cancel() }
        }
    }

    @Test fun repositoryStatusNotifiesTwoSurfacesAfterPrimaryAndRepeatAndRetainsDeletedTransactionClaim() = fixture { f ->
        val preferences = Proxy.newProxyInstance(PreferenceRepository::class.java.classLoader,
            arrayOf(PreferenceRepository::class.java)) { _, method, _ -> error("Read-only status accessed preference ${method.name}") }
            as PreferenceRepository
        val status = ObserveFixedExpensePostingStatusUseCase(FixedExpenseRepositoryImpl(f.fixed, preferences)) { 150 }
        coroutineScope {
            val left = Channel<FixedExpensePostingStatus>(Channel.UNLIMITED)
            val right = Channel<FixedExpensePostingStatus>(Channel.UNLIMITED)
            val jobs = listOf(left, right).map { channel ->
                launch { status().map { it.getValue(1) }.distinctUntilChanged().collect { channel.send(it) } }
            }
            suspend fun assertBoth(expected: FixedExpensePostingStatus) {
                for (channel in listOf(left, right)) assertEquals(expected, withTimeout(5000) { channel.receive() })
            }
            try {
                assertBoth(FixedExpensePostingStatus(100, 0, false, true))
                f.fixed.insertFullFixedExpensePosting(f.receipt)
                assertBoth(FixedExpensePostingStatus(100, 1, true, true))
                f.fixed.insertFullFixedExpensePosting(f.receipt.copy(operationId = "repeat", isRepeat = true))
                assertBoth(FixedExpensePostingStatus(100, 2, true, true))
                f.tx.physicallyDeleteTransaction(1)
                assertEquals(FixedExpensePostingStatus(100, 2, true, true), status().first().getValue(1))
                assertEquals(f.expense, f.fixed.getFixedExpenseById(1))
                assertEquals(1000L, f.tx.getSourceById(1)!!.balance)
            } finally { jobs.forEach { it.cancel() } }
        }
    }

    private fun fixture(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { f.seed(); block(f) } finally { f.close() }
    }

    private class Fixture : AutoCloseable {
        private val directory = Files.createTempDirectory("fintrack-fixed-posting-")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${directory.resolve("qa.db")}", Properties().apply {
            setProperty("foreign_keys", "true"); setProperty("busy_timeout", "5000")
        })
        val database: FinTrackDatabase
        val tx: TransactionLocalDataSourceImpl
        val fixed: FixedExpenseLocalDataSourceImpl
        val backup: BackupRepositoryImpl
        val expense = FixedExpense(id = 1, title = "QA fixed", amount = 100, categoryId = 1, sourceId = 1,
            recurrence = RecurrenceType.MONTHLY, startDate = 100, nextDueDate = 100, reminderEnabled = false)
        val transaction = Transaction(id = 1, amount = 100, categoryId = 1, sourceId = 1, timeStamp = 100, type = TransactionType.EXPENSE)
        val receipt = FixedExpensePosting("ordinary", 1, 100, 1, 10)
        init {
            FinTrackDatabase.Schema.synchronous().create(driver)
            database = FinTrackDatabase(driver, com.kazemieh.database.Asset.Adapter(EnumColumnAdapter()),
                com.kazemieh.database.Rate_cache.Adapter(EnumColumnAdapter()))
            tx = TransactionLocalDataSourceImpl(database)
            fixed = FixedExpenseLocalDataSourceImpl(database)
            backup = BackupRepositoryImpl(tx, AssetLocalDataSourceImpl(database), BudgetLocalDataSourceImpl(database),
                CheckLocalDataSourceImpl(database), DebtLocalDataSourceImpl(database), fixed, InstallmentLocalDataSourceImpl(database),
                NoteLocalDataSourceImpl(database), ShoppingLocalDataSourceImpl(database), SyncHistoryLocalDataSourceImpl(database),
                DatabaseTransactionProviderImpl(database))
        }
        suspend fun seed() {
            tx.insertFullSource(Source(id = 1, name = "QA source", balance = 1000, colorId = 0, iconId = 0))
            tx.insertFullCategory(Category(id = 1, name = "QA category", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
            fixed.insertFullFixedExpense(expense)
            tx.insertFullTransaction(transaction)
        }
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-wal", "-shm", "-journal")) Files.deleteIfExists(directory.resolve("qa.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }
}
