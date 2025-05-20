package com.kazemieh.data.datasource

import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    fun getAll(): Flow<List<TransactionWithRelations>>
    fun getByCategory(categoryId: Long): Flow<List<TransactionWithRelations>>
    fun getByFinancialSource(sourceId: Long): Flow<List<TransactionWithRelations>>
    fun getByTag(tagName: String): Flow<List<TransactionWithRelations>>
}