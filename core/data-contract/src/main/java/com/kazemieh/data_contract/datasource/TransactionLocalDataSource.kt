package com.kazemieh.data_contract.datasource

import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    suspend fun insertTransaction(transaction: Transaction, tagIds: List<Long>)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    fun getAll(): Flow<List<TransactionWithRelations>>
    fun getByCategory(categoryId: Long): Flow<List<TransactionWithRelations>>
    fun getByFinancialSource(sourceId: Long): Flow<List<TransactionWithRelations>>
//    fun getByTag(tagName: String): Flow<List<TransactionWithRelations>>
    suspend fun getAllCategory(): Flow<List<Category>>
    suspend fun getAllFinancialSource(): Flow<List<FinancialSource>>
    suspend fun getAllTag(): Flow<List<Tag>>
}