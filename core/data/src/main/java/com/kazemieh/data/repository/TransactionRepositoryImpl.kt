package com.kazemieh.data.repository

import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionWithRelations>> {
        return localDataSource.getAll()
    }

    override suspend fun insertTransaction(transaction: Transaction, tagIds: List<Long>) {
        localDataSource.insertTransaction(transaction, tagIds)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        localDataSource.delete(transaction)
    }

    override suspend fun getAllCategory(): Flow<List<Category>> {
        return localDataSource.getAllCategory()
    }

    override suspend fun getAllFinancialSource(): Flow<List<FinancialSource>> {
        return localDataSource.getAllFinancialSource()
    }

    override suspend fun getAllTag(): Flow<List<Tag>> {
        return localDataSource.getAllTag()
    }
}
