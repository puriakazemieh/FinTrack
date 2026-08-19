package com.kazemieh.data.repository

import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.domain.repository.BackupStats
import com.kazemieh.data_contract.datasource.*
import com.kazemieh.common.model.*
import kotlin.time.Clock

class BackupRepositoryImpl(
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val assetLocalDataSource: AssetLocalDataSource,
    private val budgetLocalDataSource: BudgetLocalDataSource,
    private val checkLocalDataSource: CheckLocalDataSource,
    private val debtLocalDataSource: DebtLocalDataSource,
    private val fixedExpenseLocalDataSource: FixedExpenseLocalDataSource,
    private val installmentLocalDataSource: InstallmentLocalDataSource,
    private val noteLocalDataSource: NoteLocalDataSource,
    private val shoppingLocalDataSource: ShoppingLocalDataSource,
    private val syncHistoryLocalDataSource: SyncHistoryLocalDataSource,
    private val databaseTransactionProvider: DatabaseTransactionProvider
) : BackupRepository {

    override suspend fun getBackupData(fromTimestamp: Long?, toTimestamp: Long?): BackupData {
        val allTransactions = transactionLocalDataSource.getAllTransactions()
        val filteredTransactions = if (fromTimestamp != null && toTimestamp != null) {
            allTransactions.filter { it.timeStamp in fromTimestamp..toTimestamp }
        } else {
            allTransactions
        }

        return BackupData(
            transactions = filteredTransactions,
            categories = transactionLocalDataSource.getAllCategories(),
            sources = transactionLocalDataSource.getAllSources(),
            tags = transactionLocalDataSource.getAllTags(),
            persons = transactionLocalDataSource.getAllPersons(),
            assets = assetLocalDataSource.getAllAssets(),
            budgets = budgetLocalDataSource.getAllBudgets(),
            checks = checkLocalDataSource.getAllChecks(),
            debts = debtLocalDataSource.getAllDebts(),
            fixedExpenses = fixedExpenseLocalDataSource.getAllFixedExpenses(),
            installments = installmentLocalDataSource.getAllInstallments(),
            notes = noteLocalDataSource.getAllNotes(),
            shoppingItems = shoppingLocalDataSource.getAllShoppingItems(),
            backupTimestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun getDeltaBackupData(sinceTimestamp: Long): BackupData {
        return BackupData(
            transactions = transactionLocalDataSource.getAllTransactions().filter { it.updatedAt > sinceTimestamp },
            categories = transactionLocalDataSource.getAllCategories().filter { it.updatedAt > sinceTimestamp },
            sources = transactionLocalDataSource.getAllSources().filter { it.updatedAt > sinceTimestamp },
            tags = transactionLocalDataSource.getAllTags().filter { it.updatedAt > sinceTimestamp },
            persons = transactionLocalDataSource.getAllPersons().filter { it.updatedAt > sinceTimestamp },
            assets = assetLocalDataSource.getAllAssets().filter { it.updatedAt > sinceTimestamp },
            budgets = budgetLocalDataSource.getAllBudgets().filter { it.updatedAt > sinceTimestamp },
            checks = checkLocalDataSource.getAllChecks().filter { it.updatedAt > sinceTimestamp },
            debts = debtLocalDataSource.getAllDebts().filter { it.updatedAt > sinceTimestamp },
            fixedExpenses = fixedExpenseLocalDataSource.getAllFixedExpenses().filter { it.updatedAt > sinceTimestamp },
            installments = installmentLocalDataSource.getAllInstallments().filter { it.updatedAt > sinceTimestamp },
            notes = noteLocalDataSource.getAllNotes().filter { it.updatedAt > sinceTimestamp },
            shoppingItems = shoppingLocalDataSource.getAllShoppingItems().filter { it.updatedAt > sinceTimestamp },
            backupTimestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun restoreBackupData(data: BackupData): Pair<Int, Int> = databaseTransactionProvider.runInTransaction {
        val inserted = 0
        val updated = 0
        
        handleRemoteDeletions(data)

        data.categories.filter { it.syncStatus != SyncStatus.DELETED }.forEach { transactionLocalDataSource.insertFullCategory(it) }
        data.sources.filter { it.syncStatus != SyncStatus.DELETED }.forEach { transactionLocalDataSource.insertFullSource(it) }
        data.tags.filter { it.syncStatus != SyncStatus.DELETED }.forEach { transactionLocalDataSource.insertFullTag(it) }
        data.persons.filter { it.syncStatus != SyncStatus.DELETED }.forEach { transactionLocalDataSource.insertFullPerson(it) }
        data.transactions.filter { it.syncStatus != SyncStatus.DELETED }.forEach { transactionLocalDataSource.insertFullTransaction(it) }
        data.assets.filter { it.syncStatus != SyncStatus.DELETED }.forEach { assetLocalDataSource.insertFullAsset(it) }
        data.budgets.filter { it.syncStatus != SyncStatus.DELETED }.forEach { budgetLocalDataSource.insertFullBudget(it) }
        data.checks.filter { it.syncStatus != SyncStatus.DELETED }.forEach { checkLocalDataSource.insertFullCheck(it) }
        data.debts.filter { it.syncStatus != SyncStatus.DELETED }.forEach { debtLocalDataSource.insertFullDebt(it) }
        data.fixedExpenses.filter { it.syncStatus != SyncStatus.DELETED }.forEach { fixedExpenseLocalDataSource.insertFullFixedExpense(it) }
        data.installments.filter { it.syncStatus != SyncStatus.DELETED }.forEach { installmentLocalDataSource.insertFullInstallment(it) }
        data.notes.filter { it.syncStatus != SyncStatus.DELETED }.forEach { noteLocalDataSource.insertFullNote(it) }
        data.shoppingItems.filter { it.syncStatus != SyncStatus.DELETED }.forEach { shoppingLocalDataSource.insertFullShoppingItem(it) }
        
        inserted to updated
    }

    private suspend fun handleRemoteDeletions(data: BackupData) {
        data.transactions.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            transactionLocalDataSource.physicallyDeleteTransaction(it.id)
        }
        data.categories.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            transactionLocalDataSource.physicallyDeleteCategory(it.id ?: 0)
        }
        data.sources.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            transactionLocalDataSource.physicallyDeleteSource(it.id ?: 0)
        }
        data.tags.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            transactionLocalDataSource.physicallyDeleteTag(it.id ?: 0)
        }
        data.persons.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            transactionLocalDataSource.physicallyDeletePerson(it.id ?: 0)
        }
        data.assets.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            assetLocalDataSource.physicallyDeleteAsset(it.id ?: 0)
        }
        data.budgets.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            budgetLocalDataSource.physicallyDeleteBudget(it.id ?: 0)
        }
        data.checks.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            checkLocalDataSource.physicallyDeleteCheck(it.id)
        }
        data.debts.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            debtLocalDataSource.physicallyDeleteDebt(it.id ?: 0)
        }
        data.fixedExpenses.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            fixedExpenseLocalDataSource.physicallyDeleteFixedExpense(it.id ?: 0)
        }
        data.installments.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            installmentLocalDataSource.physicallyDeleteInstallment(it.id ?: 0)
        }
        data.notes.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            noteLocalDataSource.physicallyDeleteNote(it.id ?: 0)
        }
        data.shoppingItems.filter { it.syncStatus == SyncStatus.DELETED }.forEach { 
            shoppingLocalDataSource.physicallyDeleteShoppingItem(it.id ?: 0)
        }
    }

    override suspend fun getBackupStats(): BackupStats {
        return BackupStats(
            transactionCount = transactionLocalDataSource.getAllTransactions().size,
            categoryCount = transactionLocalDataSource.getAllCategories().size,
            sourceCount = transactionLocalDataSource.getAllSources().size,
            assetCount = assetLocalDataSource.getAllAssets().size,
            tagCount = transactionLocalDataSource.getAllTags().size,
            personCount = transactionLocalDataSource.getAllPersons().size,
            noteCount = noteLocalDataSource.getAllNotes().size,
            checkCount = checkLocalDataSource.getAllChecks().size,
            debtCount = debtLocalDataSource.getAllDebts().size,
            fixedExpenseCount = fixedExpenseLocalDataSource.getAllFixedExpenses().size,
            installmentCount = installmentLocalDataSource.getAllInstallments().size,
            shoppingItemCount = shoppingLocalDataSource.getAllShoppingItems().size
        )
    }

    override suspend fun addSyncHistory(history: SyncHistory) {
        syncHistoryLocalDataSource.addSyncHistory(history)
    }

    override suspend fun getSyncHistory(limit: Long): List<SyncHistory> {
        return syncHistoryLocalDataSource.getSyncHistory(limit)
    }
}
