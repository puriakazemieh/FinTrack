package com.kazemieh.database.datasource

import com.kazemieh.common.model.*
import com.kazemieh.data_contract.datasource.*
import com.kazemieh.database.FinTrackDatabase

class SyncLocalDataSourceImpl(
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val noteLocalDataSource: NoteLocalDataSource,
    private val assetLocalDataSource: AssetLocalDataSource,
    private val budgetLocalDataSource: BudgetLocalDataSource,
    private val checkLocalDataSource: CheckLocalDataSource,
    private val debtLocalDataSource: DebtLocalDataSource,
    private val fixedExpenseLocalDataSource: FixedExpenseLocalDataSource,
    private val installmentLocalDataSource: InstallmentLocalDataSource,
    private val shoppingLocalDataSource: ShoppingLocalDataSource
) : SyncLocalDataSource {

    override suspend fun getModifiedEntities(): Map<String, List<Any>> {
        return mapOf(
            "transactions" to transactionLocalDataSource.getModifiedTransactions(),
            "categories" to transactionLocalDataSource.getModifiedCategories(),
            "sources" to transactionLocalDataSource.getModifiedSources(),
            "tags" to transactionLocalDataSource.getModifiedTags(),
            "persons" to transactionLocalDataSource.getModifiedPersons(),
            "notes" to noteLocalDataSource.getModifiedNotes(),
            "assets" to assetLocalDataSource.getModifiedAssets(),
            "budgets" to budgetLocalDataSource.getModifiedBudgets(),
            "checks" to checkLocalDataSource.getModifiedChecks(),
            "debts" to debtLocalDataSource.getModifiedDebts(),
            "fixedExpenses" to fixedExpenseLocalDataSource.getModifiedFixedExpenses(),
            "installments" to installmentLocalDataSource.getModifiedInstallments(),
            "shoppingItems" to shoppingLocalDataSource.getModifiedShoppingItems()
        )
    }

    override suspend fun markAsSynced(table: String, ids: List<Long>) {
        ids.forEach { id ->
            when (table) {
                "transactions" -> transactionLocalDataSource.markTransactionAsSynced(id)
                "categories" -> transactionLocalDataSource.markCategoryAsSynced(id)
                "sources" -> transactionLocalDataSource.markSourceAsSynced(id)
                "tags" -> transactionLocalDataSource.markTagAsSynced(id)
                "persons" -> transactionLocalDataSource.markPersonAsSynced(id)
                "notes" -> noteLocalDataSource.markNoteAsSynced(id)
                "assets" -> assetLocalDataSource.markAssetAsSynced(id)
                "budgets" -> budgetLocalDataSource.markBudgetAsSynced(id)
                "checks" -> checkLocalDataSource.markCheckAsSynced(id)
                "debts" -> debtLocalDataSource.markDebtAsSynced(id)
                "fixedExpenses" -> fixedExpenseLocalDataSource.markFixedExpenseAsSynced(id)
                "installments" -> installmentLocalDataSource.markInstallmentAsSynced(id)
                "shoppingItems" -> shoppingLocalDataSource.markShoppingItemAsSynced(id)
            }
        }
    }

    override suspend fun updateFromRemote(entities: Map<String, List<Any>>) {
        // Last-writer-wins logic is implicitly handled by the standard restoration queries
        // which use INSERT OR REPLACE and the SyncManager which compares updatedAt.
    }
}
