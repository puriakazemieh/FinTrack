package com.kazemieh.data.repository

import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.domain.repository.BackupStats
import com.kazemieh.data_contract.datasource.*
import com.kazemieh.common.model.*
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toSyncHistory
import kotlinx.datetime.Clock

class BackupRepositoryImpl(
    private val db: FinTrackDatabase,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val assetLocalDataSource: AssetLocalDataSource,
    private val budgetLocalDataSource: BudgetLocalDataSource,
    private val checkLocalDataSource: CheckLocalDataSource,
    private val debtLocalDataSource: DebtLocalDataSource,
    private val fixedExpenseLocalDataSource: FixedExpenseLocalDataSource,
    private val installmentLocalDataSource: InstallmentLocalDataSource,
    private val noteLocalDataSource: NoteLocalDataSource,
    private val shoppingLocalDataSource: ShoppingLocalDataSource
) : BackupRepository {

    private val syncQueries = db.syncHistoryQueries

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

    override suspend fun restoreBackupData(data: BackupData) {
        data.categories.forEach { transactionLocalDataSource.insertFullCategory(it) }
        data.sources.forEach { transactionLocalDataSource.insertFullSource(it) }
        data.tags.forEach { transactionLocalDataSource.insertFullTag(it) }
        data.persons.forEach { transactionLocalDataSource.insertFullPerson(it) }
        data.transactions.forEach { transactionLocalDataSource.insertFullTransaction(it) }
        data.assets.forEach { assetLocalDataSource.insertFullAsset(it) }
        data.budgets.forEach { budgetLocalDataSource.insertFullBudget(it) }
        data.checks.forEach { checkLocalDataSource.insertFullCheck(it) }
        data.debts.forEach { debtLocalDataSource.insertFullDebt(it) }
        data.fixedExpenses.forEach { fixedExpenseLocalDataSource.insertFullFixedExpense(it) }
        data.installments.forEach { installmentLocalDataSource.insertFullInstallment(it) }
        data.notes.forEach { noteLocalDataSource.insertFullNote(it) }
        data.shoppingItems.forEach { shoppingLocalDataSource.insertFullShoppingItem(it) }
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
        syncQueries.insertSyncHistory(
            timestamp = history.timestamp,
            type = history.type.name,
            status = history.status.name,
            recordCount = history.recordCount.toLong(),
            errorMessage = history.errorMessage
        )
    }

    override suspend fun getSyncHistory(limit: Long): List<SyncHistory> {
        return syncQueries.getLatestSyncHistory(limit).executeAsList().map { it.toSyncHistory() }
    }
}
