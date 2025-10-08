package com.kazemieh.domain.repository

import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insertTransaction(transaction: Transaction, tagIds: List<Long>): Long
    suspend fun deleteTransaction(transaction: Transaction)
    fun getAllTransactions(): Flow<List<TransactionWithRelations>>
    suspend fun getAllCategory(type: Int): Flow<List<Category>>
    suspend fun insertTag(tag: Tag): Long
    suspend fun insertCategory(category: Category): Long
    suspend fun insertFinancialSource(financialSource: FinancialSource): Long
    suspend fun getAllFinancialSource(): Flow<List<FinancialSource>>
    suspend fun getAllTag(): Flow<List<Tag>>
    suspend fun increaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun getDefaultCategory(type: Int): Category
    suspend fun getDefaultSource(): FinancialSource
}
