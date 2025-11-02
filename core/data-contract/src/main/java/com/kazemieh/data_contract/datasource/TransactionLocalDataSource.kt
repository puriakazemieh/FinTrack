package com.kazemieh.data_contract.datasource

import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    suspend fun insertTransaction(transaction: Transaction, tagIds: List<Long>): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    fun getAllTransactions(): Flow<List<TransactionWithRelations>>
    fun getAllTransactionsByType(type: Int): Flow<List<TransactionWithRelations>>
    fun getAllTransactionsFiltered(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<List<TransactionWithRelations>>

    fun getByCategory(categoryId: Long): Flow<List<TransactionWithRelations>>
    fun getByFinancialSource(sourceId: Long): Flow<List<TransactionWithRelations>>

    //    fun getByTag(tagName: String): Flow<List<TransactionWithRelations>>
    suspend fun insertCategory(category: Category): Long
    suspend fun insertFinancialSource(financialSource: FinancialSource): Long
    suspend fun insertTag(tag: Tag): Long
    suspend fun getAllCategory(type: Int): Flow<List<Category>>
    suspend fun getAllFinancialSource(): Flow<List<FinancialSource>>
    suspend fun getAllTag(): Flow<List<Tag>>
    suspend fun increaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun getDefaultCategory(type: Int): Category
    suspend fun getDefaultSource(): FinancialSource
}