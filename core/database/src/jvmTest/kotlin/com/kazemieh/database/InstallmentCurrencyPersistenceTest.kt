package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.*
import com.kazemieh.database.datasource.InstallmentLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import java.nio.file.Files
import java.util.Properties
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class InstallmentCurrencyPersistenceTest {
    @Test fun byIdAndRelatedReadKeepStoredCurrency() = fixture { records ->
        assertEquals("USD", records.getInstallmentById(40)!!.currencyCode)
        assertEquals("USD", records.getInstallmentWithRelations(40)!!.installment.currencyCode)
    }
    @Test fun observedListKeepsStoredCurrency() = fixture { records ->
        assertEquals("USD", records.observeInstallments().first().single().installment.currencyCode)
    }
    @Test fun editingLoadedRecordDoesNotReplaceCurrency() = fixture { records ->
        val loaded = records.getInstallmentById(40)!!
        records.updateInstallment(loaded.copy(title = "Edited", paidInstallments = 1), emptyList(), emptyList())
        assertEquals("USD", records.getAllInstallments().single().currencyCode)
        assertEquals("USD", records.getModifiedInstallments().single().currencyCode)
        assertEquals(1, records.observeInstallments().first().single().installment.paidInstallments)
    }
    private fun fixture(block: suspend (InstallmentLocalDataSourceImpl) -> Unit) = runBlocking {
        val directory = Files.createTempDirectory("installment-currency-")
        val driver = JdbcSqliteDriver("jdbc:sqlite:" + directory.resolve("qa.db"),
            Properties().apply { setProperty("foreign_keys", "true") })
        try {
            FinTrackDatabase.Schema.synchronous().create(driver)
            val db = FinTrackDatabase(driver, Asset.Adapter(EnumColumnAdapter()), Rate_cache.Adapter(EnumColumnAdapter()))
            val transactions = TransactionLocalDataSourceImpl(db)
            transactions.insertFullSource(Source(id = 1, name = "USD", balance = 10000, colorId = 1, iconId = 1, currencyCode = "USD"))
            transactions.insertFullCategory(Category(id = 1, name = "Expense", type = TransactionType.EXPENSE, colorId = 1, iconId = 1))
            val records = InstallmentLocalDataSourceImpl(db)
            records.insertFullInstallment(Installment(id = 40, title = "Saved USD", totalAmount = 3000,
                installmentAmount = 1000, totalInstallments = 3, paidInstallments = 0,
                categoryId = 1, sourceId = 1, startDate = 1000, nextDueDate = 1000,
                frequency = InstallmentFrequency.MONTHLY, currencyCode = "USD", syncStatus = SyncStatus.MODIFIED))
            block(records)
        } finally {
            driver.close()
            for (suffix in listOf("", "-journal", "-wal", "-shm")) Files.deleteIfExists(directory.resolve("qa.db$suffix"))
            Files.deleteIfExists(directory)
        }
    }
}
