package com.kazemieh.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.async.coroutines.synchronous
import com.kazemieh.common.model.*
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.datasource.*
import com.kazemieh.domain.repository.BackupData
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BackupRestoreTest {
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var database: FinTrackDatabase
    private lateinit var backupRepository: BackupRepositoryImpl
    private lateinit var transactionDataSource: TransactionLocalDataSourceImpl
    
    @Before
    fun setup() {
        val dbFile = java.io.File("test.db")
        if (dbFile.exists()) dbFile.delete()
        val props = java.util.Properties()
        props.setProperty("foreign_keys", "true")
        driver = JdbcSqliteDriver("jdbc:sqlite:test.db", props)
        FinTrackDatabase.Schema.synchronous().create(driver)
        
        database = FinTrackDatabase(
            driver = driver,
            assetAdapter = com.kazemieh.database.Asset.Adapter(
                typeAdapter = EnumColumnAdapter()
            ),
            rate_cacheAdapter = com.kazemieh.database.Rate_cache.Adapter(
                typeAdapter = EnumColumnAdapter()
            )
        )
        
        
        transactionDataSource = TransactionLocalDataSourceImpl(database)
        
        backupRepository = BackupRepositoryImpl(
            transactionLocalDataSource = transactionDataSource,
            assetLocalDataSource = AssetLocalDataSourceImpl(database),
            budgetLocalDataSource = BudgetLocalDataSourceImpl(database),
            checkLocalDataSource = CheckLocalDataSourceImpl(database),
            debtLocalDataSource = DebtLocalDataSourceImpl(database),
            fixedExpenseLocalDataSource = FixedExpenseLocalDataSourceImpl(database),
            installmentLocalDataSource = InstallmentLocalDataSourceImpl(database),
            noteLocalDataSource = NoteLocalDataSourceImpl(database),
            shoppingLocalDataSource = ShoppingLocalDataSourceImpl(database),
            syncHistoryLocalDataSource = SyncHistoryLocalDataSourceImpl(database),
            databaseTransactionProvider = DatabaseTransactionProviderImpl(database)
        )
    }
    
    @After
    fun teardown() {
        driver.close()
    }
    
    @Test
    fun `test corrupted backup restoration throws exception and rolls back`() = runBlocking {
        // First, add some valid initial data
        val initialCategory = Category(id = 1L, name = "Food", description = "", type = TransactionType.EXPENSE, colorId = 0, iconId = 0, position = 0, parentId = null, updatedAt = 0L, syncStatus = SyncStatus.SYNCED)
        transactionDataSource.insertFullCategory(initialCategory)
        
        val initialCount = transactionDataSource.getAllCategories().size
        assertEquals(1, initialCount)
        
        // Create corrupted backup data (Transaction references non-existent category 99 and source 99)
        val badTransaction = Transaction(
            id = 1L, amount = 100L, amountTransfer = 0L, categoryId = 99L, sourceId = 99L, sourceEndId = null, relatedDebtId = null, description = "", photoPath = null, timeStamp = 0L, type = TransactionType.EXPENSE, updatedAt = 0L, syncStatus = SyncStatus.SYNCED
        )
        
        val newCategory = Category(id = 2L, name = "Should Not Exist", description = "", type = TransactionType.EXPENSE, colorId = 0, iconId = 0, position = 0, parentId = null, updatedAt = 0L, syncStatus = SyncStatus.SYNCED)
        
        val corruptedBackup = BackupData(
            transactions = listOf(badTransaction),
            categories = listOf(newCategory), // Note: Category 99 is missing!
            sources = emptyList(),
            tags = emptyList(),
            persons = emptyList(),
            assets = emptyList(),
            budgets = emptyList(),
            checks = emptyList(),
            debts = emptyList(),
            fixedExpenses = emptyList(),
            installments = emptyList(),
            notes = emptyList(),
            shoppingItems = emptyList(),
            backupTimestamp = 0L
        )
        
        // We expect an exception because of foreign key constraint failure
        assertFailsWith<Exception> {
            backupRepository.restoreBackupData(corruptedBackup)
        }
        
        // VERIFY: The database should be rolled back! 
        // "Should Not Exist" category should NOT be in the database, because the transaction failed and everything should roll back.
        val categoriesAfterFailedRestore = transactionDataSource.getAllCategories()
        assertEquals(1, categoriesAfterFailedRestore.size, "Database was not rolled back after corrupted restore! Found: ${categoriesAfterFailedRestore.map { it.name }}")
    }
}
