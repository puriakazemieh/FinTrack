package com.kazemieh.database.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
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
import com.kazemieh.database.mapper.toPerson
import com.kazemieh.database.mapper.toPersonEntity
import com.kazemieh.database.mapper.toSource
import com.kazemieh.database.mapper.toSourceEntity
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

    override suspend fun update(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        val transactionRowUpdatedCount =
            transactionDao.updateTransaction(transaction.toTransactionEntity())

        tagIds.forEach { tagId ->
            transactionDao.updateTransactionTagCrossRef(
                TransactionTagCrossRef(
                    transactionId = transaction.id,
                    tagId = tagId
                )
            )
        }
        personIds.forEach { personId ->
            transactionDao.updateTransactionPersonCrossRef(
                TransactionPersonCrossRef(
                    transactionId = transaction.id,
                    personId = personId
                )
            )
        }
        return transactionRowUpdatedCount.toLong()

    }


    override fun getAllTransactionsFiltered(transactionFilterParams: TransactionFilterParams): Flow<PagingData<TransactionWithRelations>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                initialLoadSize = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                transactionDao.getAllTransactionsFiltered(
                    type = transactionFilterParams.type,
                    categoryIds = transactionFilterParams.categories.map { it.id },
                    sourceIds = transactionFilterParams.sources.map { it.id },
                    tagIds = transactionFilterParams.tags.map { it.id },
                    personIds = transactionFilterParams.persons.map { it.id },
                    fromTimestamp = transactionFilterParams.fromTimestamp,
                    toTimestamp = transactionFilterParams.toTimestamp
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { item ->
                item.toTransactionWithRelations()
            }
        }
    }

    override fun getCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> {
        return transactionDao.getCategorySums(
            type = transactionFilterParams.type,
            categoryIds = transactionFilterParams.categories.map { it.id },
            sourceIds = transactionFilterParams.sources.map { it.id },
            tagIds = transactionFilterParams.tags.map { it.id },
            personIds = transactionFilterParams.persons.map { it.id },
            fromTimestamp = transactionFilterParams.fromTimestamp,
            toTimestamp = transactionFilterParams.toTimestamp
        ).map { it.map { it.toCategory() } }
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toCategoryEntity())
    }


    override suspend fun updateCategory(category: Category): Int {
        return categoryDao.updateCategory(category.toCategoryEntity())

    }


    override suspend fun updateSource(source: Source): Int {
        return financialSourceDao.updateSource(source.toSourceEntity())

    }

    override suspend fun updateTag(tag: Tag): Int {
        return tagDao.updateTag(tag.toTagEntity())

    }

    override suspend fun updatePerson(person: Person): Int {
        return personDao.updatePerson(person.toPersonEntity())

    }

    override suspend fun deleteCategory(category: Category, moveCategory: Category?) {
        if (moveCategory != null) {
            val allTransaction = transactionDao.getAllTransactionListFiltered(
                type = category.type.count,
                categoryIds = listOf(category.id)
            )

            allTransaction.forEach { transaction ->
                transactionDao.updateTransaction(
                    transaction.transaction.copy(
                        categoryId = moveCategory.id ?: 0
                    )
                )
            }
        }

        categoryDao.deleteCategory(category.toCategoryEntity())
    }

    override suspend fun deleteTag(deleteTag: Tag, moveTag: Tag?) {
        if (moveTag != null && moveTag.id != null) {
            val allTransaction = transactionDao.getAllTransactionListFiltered(
                tagIds = listOf(deleteTag.id)
            )

            allTransaction.forEach { transaction ->
                transactionDao.updateTransactionTagCrossRef(
                    TransactionTagCrossRef(
                        transactionId = transaction.transaction.id,
                        tagId = moveTag.id!!
                    )
                )
            }
        }

        tagDao.deleteTag(deleteTag.toTagEntity())
    }

    override suspend fun deletePerson(deletePerson: Person, movePerson: Person?) {
        if (movePerson != null && movePerson.id != null) {
            val allTransaction = transactionDao.getAllTransactionListFiltered(
                personIds = listOf(deletePerson.id)
            )

            allTransaction.forEach { transaction ->
                transactionDao.updateTransactionPersonCrossRef(
                    TransactionPersonCrossRef(
                        transactionId = transaction.transaction.id,
                        personId = movePerson.id!!
                    )
                )
            }
        }

        personDao.deletePerson(deletePerson.toPersonEntity())
    }

    override suspend fun deleteSource(deleteSource: Source, moveSource: Source?) {
        if (moveSource != null) {
            val allTransaction = transactionDao.getAllTransactionListFiltered(
                sourceIds = listOf(deleteSource.id)
            )

            allTransaction.forEach { transaction ->
                transactionDao.updateTransaction(
                    transaction.transaction.copy(
                        sourceId = moveSource.id ?: 0
                    )
                )
            }
        }

        financialSourceDao.deleteSource(deleteSource.toSourceEntity())
    }

    override suspend fun insertFinancialSource(source: Source): Long {
        return financialSourceDao.insertFinancialSource(source.toSourceEntity())
    }

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(tag.toTagEntity())
    }

    override suspend fun getAllCategory(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getAllCategories(type.count).map { it.map { it.toCategory() } }
    }

    override suspend fun getAllFinancialSource(): Flow<List<Source>> {
        return financialSourceDao.getAllFinancialSources().map {
            it.map { it.toSource() }
        }
    }

    override fun getSource(sourceId: Long): Flow<Source?> {
        return financialSourceDao.getFinancialSourceById(sourceId).map { it?.toSource() }
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

    override suspend fun getDefaultCategory(type: TransactionType): Category {
        return categoryDao.getDefaultCategory(type.count).toCategory()
    }

    override suspend fun getTransferCategory(): Category {
        var transferCategory = categoryDao.getTransferCategory()?.toCategory()
        if (transferCategory == null) {
            categoryDao.createTransferCategory()
            transferCategory = categoryDao.getTransferCategory()?.toCategory()
        }
        return transferCategory!!
    }

    override suspend fun getDefaultSource(): Source? {
        return financialSourceDao.getDefaultSource()?.toSource()
    }

    override suspend fun insertPerson(person: Person): Long {
        return personDao.insertPerson(person.toPersonEntity())
    }

    override suspend fun getAllPersons(): Flow<List<Person>> {
        return personDao.getAllPersons().map { it.map { it.toPerson() } }
    }
}