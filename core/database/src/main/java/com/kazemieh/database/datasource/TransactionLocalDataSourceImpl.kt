package com.kazemieh.database.datasource

import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.dao.CategoryDao
import com.kazemieh.database.dao.FinancialSourceDao
import com.kazemieh.database.dao.TagDao
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.database.entity.TransactionTagCrossRef
import com.kazemieh.database.mapper.toCategory
import com.kazemieh.database.mapper.toFinancialSource
import com.kazemieh.database.mapper.toTag
import com.kazemieh.database.mapper.toTransactionEntity
import com.kazemieh.database.mapper.toTransactionWithRelations
import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class TransactionLocalDataSourceImpl(
    private val transactionDao: TransactionDao,
    private val tagDao: TagDao,
    private val financialSourceDao: FinancialSourceDao,
    private val categoryDao: CategoryDao,
) : TransactionLocalDataSource {

    override suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toTransactionEntity())
    }

    override suspend fun insertTransaction(
        transaction: Transaction,
        tagIds: List<Long>
    ) {
        val transactionId = transactionDao.insertTransaction(transaction.toTransactionEntity())

        tagIds?.forEach { tagId ->
            transactionDao.insertTransactionTagCrossRef(
                TransactionTagCrossRef(
                    transactionId = transactionId,
                    tagId = tagId
                )
            )
        }
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toTransactionEntity())
    }

    override fun getAll(): Flow<List<TransactionWithRelations>> = flow {
        emit(
            transactionDao.getAllTransactionsWithCategoryFinancialSourceAndTags()
                .map { it.toTransactionWithRelations() }
        )
    }

    override fun getByCategory(categoryId: Long): Flow<List<TransactionWithRelations>> = flow {
        emit(
            transactionDao.getTransactionsByCategoryId(categoryId)
                .map { it.toTransactionWithRelations() }
        )
    }

    override fun getByFinancialSource(sourceId: Long): Flow<List<TransactionWithRelations>> =
        flow {
            emit(
                transactionDao.getTransactionsByFinancialSourceId(sourceId)
                    .map { it.toTransactionWithRelations() }
            )
        }

//    override fun getByTag(tagName: String): Flow<List<TransactionWithRelations>> = flow {
//        emit(
//            transactionDao.getTransactionsByTag(tagName)
//                .map { it.toTransactionWithRelations() }
//        )
//    }

    override suspend fun getAllCategory(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { it.map { it.toCategory() } }
    }

    override suspend fun getAllFinancialSource(): Flow<List<FinancialSource>> {
        return financialSourceDao.getAllFinancialSources().map {
            it.map { it.toFinancialSource() }
        }
    }

    override suspend fun getAllTag(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { it.map { it.toTag() } }
    }

}