package com.kazemieh.database.datasource

import com.kazemieh.data.datasource.TransactionLocalDataSource
import com.kazemieh.data.mapper.toTransactionEntity
import com.kazemieh.data.mapper.toTransactionWithRelations
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransactionLocalDataSourceImpl(
    private val dao: TransactionDao
) : TransactionLocalDataSource {

    override suspend fun insert(transaction: Transaction) {
        dao.insertTransaction(transaction.toTransactionEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        dao.deleteTransaction(transaction.toTransactionEntity())
    }

    override suspend fun update(transaction: Transaction) {
        dao.updateTransaction(transaction.toTransactionEntity())
    }

    override fun getAll(): Flow<List<TransactionWithRelations>> = flow {
        emit(
            dao.getAllTransactionsWithCategoryFinancialSourceAndTags()
                .map { it.toTransactionWithRelations() }
        )
    }

    override fun getByCategory(categoryId: Long): Flow<List<TransactionWithRelations>> = flow {
        emit(
            dao.getTransactionsByCategoryId(categoryId)
                .map { it.toTransactionWithRelations() }
        )
    }

    override fun getByFinancialSource(financialSourceId: Long): Flow<List<TransactionWithRelations>> =
        flow {
            emit(
                dao.getTransactionsByFinancialSourceId(financialSourceId)
                    .map { it.toTransactionWithRelations() }
            )
        }

    override fun getByTag(tagName: String): Flow<List<TransactionWithRelations>> = flow {
        emit(
            dao.getTransactionsByTag(tagName)
                .map { it.toTransactionWithRelations() }
        )
    }
}