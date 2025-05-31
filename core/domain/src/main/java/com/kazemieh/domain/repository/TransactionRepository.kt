package com.kazemieh.domain.repository

import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    fun getAllTransactions(): Flow<List<TransactionWithRelations>>
}
