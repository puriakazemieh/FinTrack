package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.*
import com.kazemieh.database.datasource.FixedExpenseLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import java.nio.file.Files
import java.util.Properties
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class FixedExpenseAtomicPostingTest {
    @Test fun retryWritesOneTransactionBalanceReceiptAndAdvance() = fixture { f ->
        val request = f.request()
        assertTrue(f.post(request) > 0)
        assertEquals(0L, f.post(request))
        f.assertState(1, 900, 200)
    }

    @Test fun concurrentAttemptsAndLaterRetryCannotDoublePost() = fixture { f ->
        val request = f.request()
        val results = coroutineScope { (1..8).map { async { runCatching { f.post(request) } } }.awaitAll() }
        assertEquals(1, results.count { (it.getOrNull() ?: 0L) > 0L })
        assertEquals(0L, f.post(request))
        f.assertState(1, 900, 200)
    }

    @Test fun lastReceiptFailureRollsBackTransactionRelationsBalanceAndDueThenRetryWorks() = fixture { f ->
        f.tx.insertFullTag(Tag(id = 1, name = "QA tag", colorId = 0, iconId = 0))
        f.tx.insertFullPerson(Person(id = 1, name = "QA person"))
        f.db.fixedExpenseTagQueries.insertFixedExpenseTag(1, 1)
        f.db.fixedExpensePersonQueries.insertFixedExpensePerson(1, 1)
        val request = f.request()
        f.driver.execute(null, "CREATE TRIGGER fail_fixed_receipt BEFORE INSERT ON fixed_expense_posting " +
            "BEGIN SELECT RAISE(ABORT, 'injected last-write failure'); END", 0)
        assertFailsWith<Exception> { f.post(request) }
        f.assertState(0, 1000, 100)
        assertTrue(f.db.transactionTagQueries.getTagsForTransaction(1).awaitAsList().isEmpty())
        assertTrue(f.db.transactionPersonQueries.getPersonsForTransaction(1).awaitAsList().isEmpty())
        assertTrue(f.fixed.getAllFixedExpensePostings().isEmpty())
        f.driver.execute(null, "DROP TRIGGER fail_fixed_receipt", 0)
        val id = f.post(request)
        assertTrue(id > 0)
        assertEquals(1, f.db.transactionTagQueries.getTagsForTransaction(id).awaitAsList().size)
        assertEquals(1, f.db.transactionPersonQueries.getPersonsForTransaction(id).awaitAsList().size)
        f.assertState(1, 900, 200)
    }

    @Test fun confirmedRepeatAddsOneTransactionAndNoSecondAdvance() = fixture { f ->
        assertTrue(f.post(f.request()) > 0)
        val beforeRepeat = f.fixed.getFixedExpenseById(1)!!
        val request = f.request("confirmed", period = 100, repeat = true)
        assertTrue(f.post(request) > 0)
        assertEquals(0L, f.post(request))
        f.assertState(2, 800, 200)
        assertEquals(beforeRepeat, f.fixed.getFixedExpenseById(1))
        val receipts = f.fixed.getAllFixedExpensePostings()
        assertEquals(setOf(false, true), receipts.map { it.isRepeat }.toSet())
        assertEquals(setOf(100L), receipts.map { it.periodDueDate }.toSet())
    }

    @Test fun nextOccurrenceCanPostOnceIndependently() = fixture { f ->
        assertTrue(f.post(f.request()) > 0)
        assertTrue(f.post(f.request("next", period = 200, next = 300)) > 0)
        f.assertState(2, 800, 300)
        assertEquals(setOf(100L, 200L), f.fixed.getAllFixedExpensePostings().map { it.periodDueDate }.toSet())
    }

    @Test fun repeatRequiresAnExistingOrdinaryReceipt() = fixture { f ->
        assertEquals(0L, f.post(f.request(repeat = true)))
        f.assertState(0, 1000, 100)
    }

    @Test fun inactiveDeletedAndMissingExpensesCannotPost() = fixture { f ->
        val request = f.request()
        f.fixed.updateFixedExpense(request.expense.copy(isActive = false))
        assertEquals(0L, f.post(request))
        assertEquals(0L, f.post(f.request()))
        f.fixed.deleteFixedExpense(1)
        assertEquals(0L, f.post(request))
        assertEquals(0L, f.post(request.copy(expense = request.expense.copy(id = 999))))
        assertEquals(0L, f.tx.getTransactionCount())
        assertEquals(1000L, f.tx.getSourceById(1)!!.balance)
    }

    @Test fun staleExpenseOrChangedRelationsRejectsBeforeFinancialWrites() = fixture { f ->
        val old = f.request()
        f.fixed.updateFixedExpense(old.expense.copy(title = "Changed after click"))
        assertEquals(0L, f.post(old))
        val beforeRelation = f.request()
        f.tx.insertFullTag(Tag(id = 1, name = "new tag", colorId = 0, iconId = 0))
        f.db.fixedExpenseTagQueries.insertFixedExpenseTag(1, 1)
        assertEquals(0L, f.post(beforeRelation))
        f.assertState(0, 1000, 100)
    }

    @Test fun mismatchedMoneyCategorySourceAndTransactionTypeAreRejected() = fixture { f ->
        val request = f.request()
        f.tx.insertFullSource(Source(id = 2, name = "Other", balance = 1000, colorId = 0, iconId = 0))
        f.tx.insertFullCategory(Category(id = 2, name = "Other", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
        for (invalid in listOf(f.transaction.copy(amount = 101), f.transaction.copy(amount = 0),
            f.transaction.copy(currencyCode = "USD"), f.transaction.copy(categoryId = 2),
            f.transaction.copy(sourceId = 2), f.transaction.copy(type = TransactionType.INCOME),
            f.transaction.copy(description = "not this cost"), f.transaction.copy(relatedDebtId = 1))) {
            assertEquals(0L, f.post(request, invalid))
        }
        f.assertState(0, 1000, 100)
    }

    @Test fun sourceCurrencyMismatchDeletedSourceAndOverflowAreRejected() = fixture { f ->
        val request = f.request()
        val source = f.tx.getSourceById(1)!!
        f.tx.updateSource(source.copy(currencyCode = "USD"))
        assertEquals(0L, f.post(request))
        f.tx.updateSource(source.copy(balance = Long.MIN_VALUE))
        assertEquals(0L, f.post(request))
        f.driver.execute(null, "UPDATE source SET syncStatus=2 WHERE id=1", 0)
        assertEquals(0L, f.post(request))
        assertEquals(0L, f.tx.getTransactionCount())
        assertTrue(f.fixed.getAllFixedExpensePostings().isEmpty())
        assertEquals(100L, f.fixed.getFixedExpenseById(1)!!.nextDueDate)
    }

    @Test fun matchingForeignCurrencyIsPreserved() = fixture { f ->
        f.fixed.updateFixedExpense(f.fixed.getFixedExpenseById(1)!!.copy(currencyCode = "USD"))
        f.tx.updateSource(f.tx.getSourceById(1)!!.copy(currencyCode = "USD"))
        assertTrue(f.post(f.request(), f.transaction.copy(currencyCode = "USD")) > 0)
        assertEquals("USD", f.tx.getAllTransactions().single().currencyCode)
        f.assertState(1, 900, 200)
    }

    @Test fun oneTimeCompletesOnceAndConfirmedRepeatKeepsItCompleted() = fixture { f ->
        f.fixed.updateFixedExpense(f.fixed.getFixedExpenseById(1)!!.copy(recurrence = RecurrenceType.ONCE))
        val request = f.request(next = 100, active = false)
        assertTrue(f.post(request) > 0)
        assertFalse(f.fixed.getFixedExpenseById(1)!!.isActive)
        assertEquals(0L, f.post(request))
        assertTrue(f.post(f.request("confirmed", period = 100, next = 100, active = false, repeat = true)) > 0)
        assertFalse(f.fixed.getFixedExpenseById(1)!!.isActive)
        f.assertState(2, 800, 100)
    }

    @Test fun invalidAdvancementOrOtherPeriodCannotPost() = fixture { f ->
        for (request in listOf(f.request(period = 99), f.request(next = 99), f.request(next = 100)))
            assertEquals(0L, f.post(request))
        f.assertState(0, 1000, 100)
    }

    @Test fun deletingTransactionOrResettingDueDoesNotRemovePeriodClaim() = fixture { f ->
        val request = f.request()
        val id = f.post(request)
        f.tx.physicallyDeleteTransaction(id)
        f.fixed.updateFixedExpense(request.expense)
        assertEquals(0L, f.post(f.request("another ordinary attempt")))
        assertEquals(0L, f.tx.getTransactionCount())
        assertEquals(900L, f.tx.getSourceById(1)!!.balance)
        assertEquals(1, f.fixed.getAllFixedExpensePostings().size)
    }

    private fun fixture(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { f.seed(); block(f) } finally { f.close() }
    }
    private class Fixture : AutoCloseable {
        private val directory = Files.createTempDirectory("fintrack-fixed-atomic-")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${directory.resolve("qa.db")}", Properties().apply {
            setProperty("foreign_keys", "true"); setProperty("busy_timeout", "5000")
        })
        val db: FinTrackDatabase
        val tx: TransactionLocalDataSourceImpl
        val fixed: FixedExpenseLocalDataSourceImpl
        val transaction = Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 1, description = "QA fixed",
            timeStamp = 1000, type = TransactionType.EXPENSE)
        init {
            FinTrackDatabase.Schema.synchronous().create(driver)
            db = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            tx = TransactionLocalDataSourceImpl(db)
            fixed = FixedExpenseLocalDataSourceImpl(db)
        }
        suspend fun seed() {
            tx.insertFullSource(Source(id = 1, name = "QA", balance = 1000, colorId = 0, iconId = 0))
            tx.insertFullCategory(Category(id = 1, name = "QA", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
            fixed.insertFullFixedExpense(FixedExpense(id = 1, title = "QA fixed", amount = 100, categoryId = 1, sourceId = 1,
                recurrence = RecurrenceType.MONTHLY, startDate = 100, nextDueDate = 100, reminderEnabled = false))
        }
        suspend fun request(operation: String = "ordinary", period: Long = 100, next: Long = 200,
            active: Boolean = true, repeat: Boolean = false) = FixedExpensePostingRequest(operation,
            fixed.getAllFixedExpenses().single(), period, next, active, repeat)
        suspend fun post(request: FixedExpensePostingRequest, transaction: Transaction = this.transaction) =
            tx.addFixedExpenseTransactionWithBalance(request, transaction)
        suspend fun assertState(count: Long, balance: Long, due: Long) {
            assertEquals(count, tx.getTransactionCount())
            assertEquals(balance, tx.getSourceById(1)!!.balance)
            assertEquals(due, fixed.getFixedExpenseById(1)!!.nextDueDate)
            assertEquals(count.toInt(), fixed.getAllFixedExpensePostings().size)
        }
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-wal", "-shm", "-journal")) Files.deleteIfExists(directory.resolve("qa.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }
}
