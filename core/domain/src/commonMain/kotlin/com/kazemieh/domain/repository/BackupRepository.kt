package com.kazemieh.domain.repository

import com.kazemieh.common.model.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val sources: List<Source>,
    val tags: List<Tag>,
    val persons: List<Person>,
    val assets: List<Asset>,
    val budgets: List<Budget>,
    val checks: List<Check>,
    val debts: List<Debt>,
    val fixedExpenses: List<FixedExpense>,
    val installments: List<Installment>,
    val notes: List<Note>,
    val shoppingItems: List<ShoppingItem>,
    val backupTimestamp: Long
)

@Serializable
data class BackupStats(
    val transactionCount: Int,
    val categoryCount: Int,
    val sourceCount: Int,
    val assetCount: Int,
    val tagCount: Int,
    val personCount: Int,
    val noteCount: Int,
    val checkCount: Int,
    val debtCount: Int,
    val fixedExpenseCount: Int,
    val installmentCount: Int,
    val shoppingItemCount: Int
)

interface BackupRepository {
    suspend fun getBackupData(fromTimestamp: Long? = null, toTimestamp: Long? = null): BackupData
    suspend fun getDeltaBackupData(sinceTimestamp: Long): BackupData
    suspend fun restoreBackupData(data: BackupData): Pair<Int, Int> // Returns (inserted, updated)
    suspend fun getBackupStats(): BackupStats
    
    suspend fun addSyncHistory(history: SyncHistory)
    suspend fun getSyncHistory(limit: Long = 10): List<SyncHistory>
}
