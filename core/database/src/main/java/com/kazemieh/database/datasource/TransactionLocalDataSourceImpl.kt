package com.kazemieh.database.datasource

import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.dao.CategoryDao
import com.kazemieh.database.dao.FinancialSourceDao
import com.kazemieh.database.dao.TagDao
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.database.entity.TransactionTagCrossRef
import com.kazemieh.database.mapper.toCategory
import com.kazemieh.database.mapper.toCategoryEntity
import com.kazemieh.database.mapper.toFinancialSource
import com.kazemieh.database.mapper.toFinancialSourceEntity
import com.kazemieh.database.mapper.toTag
import com.kazemieh.database.mapper.toTagEntity
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
    ): Long {
        val transactionId = transactionDao.insertTransaction(transaction.toTransactionEntity())

        tagIds.forEach { tagId ->
            transactionDao.insertTransactionTagCrossRef(
                TransactionTagCrossRef(
                    transactionId = transactionId,
                    tagId = tagId
                )
            )
        }
        return transactionId
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toTransactionEntity())
    }

    override fun getAllTransactions(): Flow<List<TransactionWithRelations>> {
        return transactionDao.getAllTransactionsWithCategoryFinancialSourceAndTags().map {
            it.map { it.toTransactionWithRelations() }
        }
    }

    override fun getAllTransactionsByType(type: Int): Flow<List<TransactionWithRelations>> {
        return transactionDao.getAllTransactionsByTypeWithCategoryFinancialSourceAndTags(type).map {
            it.map { it.toTransactionWithRelations() }
        }
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

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toCategoryEntity())
    }

    override suspend fun insertFinancialSource(financialSource: FinancialSource): Long {
        return financialSourceDao.insertFinancialSource(financialSource.toFinancialSourceEntity())
    }

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(tag.toTagEntity())
    }

//    override fun getByTag(tagName: String): Flow<List<TransactionWithRelations>> = flow {
//        emit(
//            transactionDao.getTransactionsByTag(tagName)
//                .map { it.toTransactionWithRelations() }
//        )
//    }

    override suspend fun getAllCategory(type: Int): Flow<List<Category>> {
        return categoryDao.getAllCategories(type).map { it.map { it.toCategory() } }
    }

    override suspend fun getAllFinancialSource(): Flow<List<FinancialSource>> {
        return financialSourceDao.getAllFinancialSources().map {
            it.map { it.toFinancialSource() }
        }
    }

    override suspend fun getAllTag(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { it.map { it.toTag() } }
    }

    override suspend fun increaseBalanceFinancialSource(id: Long, amount: Int) {
        financialSourceDao.increaseBalance(id, amount)
    }

    override suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int) {
        financialSourceDao.decreaseBalance(id, amount)
    }

    override suspend fun getDefaultCategory(type: Int): Category {
        return categoryDao.getDefaultCategory(type).toCategory()
    }

    override suspend fun getDefaultSource(): FinancialSource {
        return financialSourceDao.getDefaultSource().toFinancialSource()
    }

}