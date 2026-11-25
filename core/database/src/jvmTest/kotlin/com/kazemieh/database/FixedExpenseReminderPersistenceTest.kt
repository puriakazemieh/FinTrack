package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.datasource.FixedExpenseLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

class FixedExpenseReminderPersistenceTest {
    @Test fun byIdAndBackupReadsKeepTheSavedCurrency() = fixture { f ->
        assertEquals("USD", f.expenses.getFixedExpenseById(40L)!!.currencyCode)
        assertEquals("USD", f.expenses.getModifiedFixedExpenses().single().currencyCode)
    }

    @Test fun listReadsKeepTheSavedCurrency() = fixture { f ->
        assertEquals("USD", f.expenses.getAllFixedExpenses().single().currencyCode)
        assertEquals("USD", f.expenses.observeAllFixedExpenses().first().single().currencyCode)
    }

    @Test fun filteredReadsKeepTheSavedCurrency() = fixture { f ->
        val saved = f.expenses.observeFixedExpensesFiltered("Scheduled", listOf(1L), listOf(1L), emptyList(), emptyList()).first().single()
        assertEquals("USD", saved.currencyCode)
    }

    @Test fun insertUpdateAndColdReopenKeepReminderConfiguration() = fixture { f ->
        val firstTime = 1800000000000L
        val id = f.expenses.insertFixedExpense(f.original.copy(id = 0L, title = "Inserted reminder", reminderEnabled = true, reminderTime = firstTime))
        assertEquals(41L, id)
        assertReminder(f.expenses.getFixedExpenseById(id)!!, id, true, firstTime)
        val saved = f.expenses.getFixedExpenseById(id)!!
        f.expenses.updateFixedExpense(saved.copy(reminderEnabled = false, reminderTime = firstTime + 3600000L))
        f.reopen()
        val disabled = f.expenses.getFixedExpenseById(id)!!
        assertReminder(disabled, id, false, firstTime + 3600000L)
        assertEquals(saved.nextDueDate, disabled.nextDueDate)
        assertEquals(saved.isAutoPostEnabled, disabled.isAutoPostEnabled)
        assertEquals(1000L, f.transactions.getSourceById(1L)!!.balance)
        assertEquals(0L, f.transactions.getTransactionCount())
    }

    @Test fun fullRestoreAndEveryReadPathKeepDisabledReminderAndCurrency() = fixture { f ->
        val time = 1800000000000L
        f.expenses.insertFullFixedExpense(f.original.copy(id = 50L, title = "Restored reminder", reminderEnabled = false, reminderTime = time))
        f.reopen()
        assertReminder(f.expenses.getFixedExpenseById(50L)!!, 50L, false, time)
        assertReminder(f.expenses.getModifiedFixedExpenses().single { it.id == 50L }, 50L, false, time)
        assertReminder(f.expenses.getAllFixedExpenses().single { it.id == 50L }, 50L, false, time)
        assertReminder(f.expenses.observeAllFixedExpenses().first().single { it.id == 50L }, 50L, false, time)
        assertReminder(f.expenses.observeFixedExpensesFiltered("Restored", listOf(1L), listOf(1L), emptyList(), emptyList()).first().single(), 50L, false, time)
    }

    @Test fun oldStyleSqlInsertKeepsLegacyReminderDefaults() = fixture { f ->
        f.driver.execute(null, "INSERT INTO fixed_expense(id,title,amount,categoryId,sourceId,recurrence,startDate,nextDueDate,currencyCode) " +
            "VALUES(70,'Legacy writer',123,1,1,'MONTHLY',1700000000000,1700000000000,'USD')", 0)
        assertReminder(f.expenses.getFixedExpenseById(70L)!!, 70L, true, null)
    }

    @Test fun legacyBackupGetsCompatibleReminderDefaults() {
        val expense = Json.decodeFromString(FixedExpense.serializer(), legacyBackup)
        assertTrue(expense.reminderEnabled)
        assertNull(expense.reminderTime)
        assertEquals("USD", expense.currencyCode)
    }

    @Test fun newBackupKeepsDisabledReminderAndExplicitTime() {
        val expense = Json.decodeFromString(FixedExpense.serializer(), legacyBackup)
            .copy(reminderEnabled = false, reminderTime = 1800000000000L)
        val encoded = Json.encodeToString(FixedExpense.serializer(), expense)
        assertEquals(expense, Json.decodeFromString(FixedExpense.serializer(), encoded))
    }

    private val legacyBackup = """{"id":40,"title":"Legacy expense","amount":123,"categoryId":1,"sourceId":1,"recurrence":"MONTHLY","startDate":1700000000000,"nextDueDate":1700000000000,"currencyCode":"USD"}"""

    private fun assertReminder(saved: FixedExpense, id: Long, enabled: Boolean, time: Long?) {
        assertEquals(id, saved.id)
        assertEquals(enabled, saved.reminderEnabled)
        assertEquals(time, saved.reminderTime)
        assertEquals("USD", saved.currencyCode)
        assertEquals(123L, saved.amount)
    }

    private fun fixture(block: suspend (Fixture) -> Unit) = runBlocking {
        val f = Fixture()
        try { block(f) } finally { f.close() }
    }

    private class Fixture : AutoCloseable {
        private val directory = Files.createTempDirectory("fixed-reminder-persistence-")
        private val url = "jdbc:sqlite:" + directory.resolve("qa.db")
        private fun openDriver() = JdbcSqliteDriver(url, Properties().apply { setProperty("foreign_keys", "true") })
        var driver = openDriver()
        private fun database() = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
        var db: FinTrackDatabase
        var transactions: TransactionLocalDataSourceImpl
        var expenses: FixedExpenseLocalDataSourceImpl
        val original = FixedExpense(
            id = 40L, title = "Scheduled expense", amount = 123L, categoryId = 1L, sourceId = 1L,
            recurrence = RecurrenceType.MONTHLY, startDate = 1700000000000L, nextDueDate = 1700000000000L,
            currencyCode = "USD", syncStatus = SyncStatus.MODIFIED
        )
        init {
            FinTrackDatabase.Schema.synchronous().create(driver)
            db = database()
            transactions = TransactionLocalDataSourceImpl(db)
            expenses = FixedExpenseLocalDataSourceImpl(db)
            runBlocking {
                transactions.insertFullSource(Source(id = 1L, name = "USD source", balance = 1000L, colorId = 1, iconId = 1, currencyCode = "USD"))
                transactions.insertFullCategory(Category(id = 1L, name = "Expense", type = TransactionType.EXPENSE, colorId = 1, iconId = 1))
                expenses.insertFullFixedExpense(original)
            }
        }
        fun reopen() {
            driver.close()
            driver = openDriver()
            db = database()
            transactions = TransactionLocalDataSourceImpl(db)
            expenses = FixedExpenseLocalDataSourceImpl(db)
        }
        override fun close() {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(directory.resolve("qa.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }
}
