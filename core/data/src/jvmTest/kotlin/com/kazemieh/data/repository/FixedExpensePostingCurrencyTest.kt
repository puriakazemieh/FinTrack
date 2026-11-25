package com.kazemieh.data.repository

import com.kazemieh.common.model.*
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.PreferenceRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class FixedExpensePostingCurrencyTest {
    @Test fun postingPreservesStoredCurrencyWithoutReadingCurrentPreference() = runBlocking {
        val expense = FixedExpense(id = 7, title = "USD cost", amount = 100, categoryId = 1, sourceId = 1,
            recurrence = RecurrenceType.MONTHLY, startDate = 1, nextDueDate = 1, currencyCode = "USD")
        val request = FixedExpensePostingRequest("ordinary", expense, 1, 2, true)
        val transaction = Transaction(id = 0, amount = 100, currencyCode = "USD", categoryId = 1,
            sourceId = 1, type = TransactionType.EXPENSE)
        var writes = 0
        val local = proxy<TransactionLocalDataSource> { name, args ->
            assertEquals("addFixedExpenseTransactionWithBalance", name)
            assertSame(request, args[0]); assertSame(transaction, args[1])
            writes++; 9L
        }
        val preferences = proxy<PreferenceRepository> { name, args ->
            check(name == "getString" && args[0] == "recent_searches") { "Unexpected preference access $name" }
            ""
        }
        assertEquals(9L, TransactionRepositoryImpl(local, preferences).addFixedExpenseTransactionWithBalance(request, transaction))
        assertEquals(1, writes)
    }
    private inline fun <reified T> proxy(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
