package com.kazemieh.database.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.FinancialSource
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.dao.CategoryDao
import com.kazemieh.database.dao.FinancialSourceDao
import com.kazemieh.database.dao.PersonDao
import com.kazemieh.database.dao.TagDao
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.database.entity.TransactionPersonCrossRef
import com.kazemieh.database.entity.TransactionTagCrossRef
import com.kazemieh.database.mapper.toCategory
import com.kazemieh.database.mapper.toCategoryEntity
import com.kazemieh.database.mapper.toFinancialSource
import com.kazemieh.database.mapper.toFinancialSourceEntity
import com.kazemieh.database.mapper.toPerson
import com.kazemieh.database.mapper.toPersonEntity
import com.kazemieh.database.mapper.toTag
import com.kazemieh.database.mapper.toTagEntity
import com.kazemieh.database.mapper.toTransactionEntity
import com.kazemieh.database.mapper.toTransactionWithRelations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionLocalDataSourceImpl(
    private val transactionDao: TransactionDao,
    private val tagDao: TagDao,
    private val financialSourceDao: FinancialSourceDao,
    private val categoryDao: CategoryDao,
    private val personDao: PersonDao,
) : TransactionLocalDataSource {

    override suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toTransactionEntity())
    }

    override suspend fun insertTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
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
        personIds.forEach { personId ->
            transactionDao.insertTransactionPersonCrossRef(
                TransactionPersonCrossRef(
                    transactionId = transactionId,
                    personId = personId
                )
            )
        }
        return transactionId
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toTransactionEntity())
    }


    override fun getAllTransactions(): Flow<PagingData<TransactionWithRelations>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                initialLoadSize = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                transactionDao.getAllTransactionsWithCategoryFinancialSourceAndTags()
            }
        ).flow.map { pagingData ->
            pagingData.map { item ->
                item.toTransactionWithRelations()
            }
        }
    }

    override fun getAllTransactionsByType(type: Int): Flow<List<TransactionWithRelations>> {
        return transactionDao.getAllTransactionsByTypeWithCategoryFinancialSourceAndTags(type).map {
            it.map { it.toTransactionWithRelations() }
        }
    }

    override fun getAllTransactionsFiltered(
        type: Int?,
        categoryIds: List<Int>,
        sourceIds: List<Int>,
        tagIds: List<Int>,
        personIds: List<Int>,
        fromTimestamp: Long?,
        toTimestamp: Long?
    ): Flow<PagingData<TransactionWithRelations>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                initialLoadSize = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                transactionDao.getAllTransactionsFiltered(
                    type = type,
                    categoryIds = categoryIds,
                    sourceIds = sourceIds,
                    tagIds = tagIds,
                    personIds = personIds,
                    fromTimestamp = fromTimestamp,
                    toTimestamp = toTimestamp
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { item ->
                item.toTransactionWithRelations()
            }
        }
    }

    override fun getCategorySums(
        type: Int?,
        categoryIds: List<Int>,
        sourceIds: List<Int>,
        tagIds: List<Int>,
        personIds: List<Int>,
        fromTimestamp: Long?,
        toTimestamp: Long?
    ): Flow<List<CategorySum>> {
        return transactionDao.getCategorySums(
            type = type,
            categoryIds = categoryIds,
            sourceIds = sourceIds,
            tagIds = tagIds,
            personIds = personIds,
            fromTimestamp = fromTimestamp,
            toTimestamp = toTimestamp
        ).map {
            it.map { it.toCategory() }
        }
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

    override suspend fun getTransferCategory(): Category {
        return categoryDao.getTransferCategory().toCategory()
    }

    override suspend fun getDefaultSource(): FinancialSource {
        return financialSourceDao.getDefaultSource().toFinancialSource()
    }

    override suspend fun insertPerson(person: Person): Long {
        return personDao.insertPerson(person.toPersonEntity())
    }

    override suspend fun getAllPersons(): Flow<List<Person>> {
        return personDao.getAllPersons().map { it.map { it.toPerson() } }
    }
}