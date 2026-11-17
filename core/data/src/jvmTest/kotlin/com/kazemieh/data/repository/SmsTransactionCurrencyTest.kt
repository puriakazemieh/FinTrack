package com.kazemieh.data.repository

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.PreferenceRepository
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Proxy
import kotlin.test.*

class SmsTransactionCurrencyTest {
    @Test fun changedActiveCurrencyCannotSilentlyRelabelSmsMoney() = runBlocking {
        var active = "IRT"
        var writes = 0
        val preferences = proxy<PreferenceRepository> { name, args ->
            if (name == "getString") {
                if (args[0] == "PREF_CURRENCY") active else args[1]
            } else error("Unexpected preference access: $name")
        }
        val local = proxy<TransactionLocalDataSource> { name, args ->
            check(name == "addSmsTransactionWithBalance")
            assertEquals(42L, args[0])
            assertEquals(active, (args[1] as Transaction).currencyCode)
            writes++
            9L
        }
        val repository = TransactionRepositoryImpl(local, preferences)
        val transaction = Transaction(id = 0, amount = 100, categoryId = 1, sourceId = 1, type = TransactionType.EXPENSE)
        assertEquals(9L, repository.addSmsTransactionWithBalance(42, transaction, emptyList(), emptyList(), mapOf(1L to -100L)))
        active = "IRR"
        assertEquals(0L, repository.addSmsTransactionWithBalance(42, transaction, emptyList(), emptyList(), mapOf(1L to -100L)))
        assertEquals(1, writes)
        assertEquals(9L, repository.addSmsTransactionWithBalance(42, transaction.copy(currencyCode = "IRR"), emptyList(), emptyList(), mapOf(1L to -100L)))
        assertEquals(2, writes)
    }

    private inline fun <reified T> proxy(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
