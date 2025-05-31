package com.kazemieh.data.repository

import com.kazemieh.data.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionWithRelations>> {
        return localDataSource.getAll()
    }

    override suspend fun addTransaction(transaction: Transaction) {
        localDataSource.insert(transaction)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        localDataSource.delete(transaction)
    }
}
