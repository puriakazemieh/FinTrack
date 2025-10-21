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
        return localDataSource.getAllTransactions()
    }
    override fun getAllTransactionsByType(type: Int): Flow<List<TransactionWithRelations>> {
        return localDataSource.getAllTransactionsByType(type)
    }

    override suspend fun insertTransaction(transaction: Transaction, tagIds: List<Long>): Long {
        return localDataSource.insertTransaction(transaction, tagIds)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        localDataSource.delete(transaction)
    }

    override suspend fun getAllCategory(type: Int): Flow<List<Category>> {
        return localDataSource.getAllCategory(type)
    }

    override suspend fun insertTag(tag: Tag): Long {
        return localDataSource.insertTag(tag)
    }

    override suspend fun insertCategory(category: Category): Long {
        return localDataSource.insertCategory(category)
    }

    override suspend fun insertFinancialSource(financialSource: FinancialSource): Long {
        return localDataSource.insertFinancialSource(financialSource)
    }

    override suspend fun getAllFinancialSource(): Flow<List<FinancialSource>> {
        return localDataSource.getAllFinancialSource()
    }

    override suspend fun getAllTag(): Flow<List<Tag>> {
        return localDataSource.getAllTag()
    }

    override suspend fun increaseBalanceFinancialSource(id: Long, amount: Int) {
        localDataSource.increaseBalanceFinancialSource(id, amount)
    }

    override suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int) {
        localDataSource.decreaseBalanceFinancialSource(id, amount)
    }

    override suspend fun getDefaultCategory(type: Int): Category {
        return localDataSource.getDefaultCategory(type)
    }

    override suspend fun getDefaultSource(): FinancialSource {
        return localDataSource.getDefaultSource()
    }
}
