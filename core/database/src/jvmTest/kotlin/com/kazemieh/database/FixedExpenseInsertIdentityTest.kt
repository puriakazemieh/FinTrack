package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.datasource.FixedExpenseLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FixedExpenseInsertIdentityTest {
    @Test fun noRelationsReturnsTheInsertedExpenseId() = assertIdentity(emptyList(), emptyList())
    @Test fun tagRelationsCannotReplaceTheExpenseId() = assertIdentity(listOf(1L, 2L), emptyList())
    @Test fun personRelationsCannotReplaceTheExpenseId() = assertIdentity(emptyList(), listOf(1L, 2L, 3L))
    @Test fun mixedRelationsKeepTheParentIdentityAndAssociations() = assertIdentity(listOf(1L, 2L), listOf(1L))

    @Test fun invalidRelationRollsBackTheParentAndValidRelationsBeforeRetry() = fixture { db, transactions, expenses ->
        val failed = runCatching { expenses.insertFixedExpense(expense().copy(tagIds = listOf(1L), personIds = listOf(999L))) }
        assertTrue(failed.isFailure)
        assertNull(expenses.getFixedExpenseById(101L))
        assertTrue(db.fixedExpenseTagQueries.getTagsForFixedExpense(101L).awaitAsList().isEmpty())
        assertEquals(1, expenses.getAllFixedExpenses().size)
        val id = expenses.insertFixedExpense(expense().copy(tagIds = listOf(1L), personIds = listOf(1L)))
        assertEquals(101L, id)
        assertEquals(listOf(1L), db.fixedExpenseTagQueries.getTagsForFixedExpense(id).awaitAsList().map { it.id })
        assertEquals(listOf(1L), db.fixedExpensePersonQueries.getPersonsForFixedExpense(id).awaitAsList().map { it.id })
        assertEquals(1000L, transactions.getSourceById(1L)!!.balance)
        assertEquals(0L, transactions.getTransactionCount())
    }

    private fun assertIdentity(tags: List<Long>, persons: List<Long>) = fixture { db, transactions, expenses ->
        val id = expenses.insertFixedExpense(expense().copy(tagIds = tags, personIds = persons))
        assertEquals(101L, id, "Return the parent ID even after relation-table inserts")
        assertEquals("New expense", expenses.getFixedExpenseById(id)!!.title)
        assertEquals("Existing expense", expenses.getFixedExpenseById(100L)!!.title)
        assertEquals(tags.sorted(), db.fixedExpenseTagQueries.getTagsForFixedExpense(id).awaitAsList().map { it.id }.sorted())
        assertEquals(persons.sorted(), db.fixedExpensePersonQueries.getPersonsForFixedExpense(id).awaitAsList().map { it.id }.sorted())
        assertEquals(102L, expenses.insertFixedExpense(expense().copy(title = "Next expense")))
        assertEquals(3, expenses.getAllFixedExpenses().size)
        assertEquals(1000L, transactions.getSourceById(1L)!!.balance)
        assertEquals(0L, transactions.getTransactionCount())
    }

    private fun expense() = FixedExpense(
        title = "New expense", amount = 2000L, categoryId = 1L, sourceId = 1L,
        recurrence = RecurrenceType.MONTHLY, startDate = 1700000000000L, nextDueDate = 1700000000000L
    )

    private fun fixture(block: suspend (FinTrackDatabase, TransactionLocalDataSourceImpl, FixedExpenseLocalDataSourceImpl) -> Unit) = runBlocking {
        val directory = Files.createTempDirectory("fixed-expense-identity-")
        val driver = JdbcSqliteDriver("jdbc:sqlite:" + directory.resolve("qa.db"), Properties().apply { setProperty("foreign_keys", "true") })
        try {
            FinTrackDatabase.Schema.synchronous().create(driver)
            val db = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            val transactions = TransactionLocalDataSourceImpl(db)
            val expenses = FixedExpenseLocalDataSourceImpl(db)
            transactions.insertFullSource(Source(id = 1L, name = "Financial", balance = 1000L, colorId = 1, iconId = 1))
            transactions.insertFullCategory(Category(id = 1L, name = "Expense", type = TransactionType.EXPENSE, colorId = 1, iconId = 1))
            for (id in 1L..2L) transactions.insertFullTag(Tag(id = id, name = "Tag $id", colorId = 1, iconId = 1))
            for (id in 1L..3L) transactions.insertFullPerson(Person(id = id, name = "Person $id"))
            expenses.insertFullFixedExpense(expense().copy(id = 100L, title = "Existing expense"))
            block(db, transactions, expenses)
        } finally {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(directory.resolve("qa.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }
}
