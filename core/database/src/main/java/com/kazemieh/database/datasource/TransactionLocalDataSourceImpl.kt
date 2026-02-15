package com.kazemieh.database.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
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
import com.kazemieh.database.DatabaseModule
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
    private val db: DatabaseModule,
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
    ): Long = db.withTransaction {

        val transactionId = transactionDao.insertTransaction(transaction.toTransactionEntity())

        val distinctTags = tagIds.distinct()
        val distinctPersons = personIds.distinct()

        if (distinctTags.isNotEmpty()) {
            transactionDao.insertTransactionTagCrossRefs(
                distinctTags.map { TransactionTagCrossRef(transactionId, it) }
            )
        }

        if (distinctPersons.isNotEmpty()) {
            transactionDao.insertTransactionPersonCrossRefs(
                distinctPersons.map { TransactionPersonCrossRef(transactionId, it) }
            )
        }

        transactionId
    }


    override suspend fun update(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long = db.withTransaction {

        val rowCount = transactionDao.updateTransaction(transaction.toTransactionEntity())

        transactionDao.deleteAllTagRefsForTransaction(transaction.id)
        transactionDao.deleteAllPersonRefsForTransaction(transaction.id)

        val distinctTags = tagIds.distinct()
        val distinctPersons = personIds.distinct()

        if (distinctTags.isNotEmpty()) {
            transactionDao.insertTransactionTagCrossRefs(
                distinctTags.map { TransactionTagCrossRef(transaction.id, it) }
            )
        }

        if (distinctPersons.isNotEmpty()) {
            transactionDao.insertTransactionPersonCrossRefs(
                distinctPersons.map { TransactionPersonCrossRef(transaction.id, it) }
            )
        }

        rowCount.toLong()
    }


    override fun getAllTransactionsFiltered(transactionFilterParams: TransactionFilterParams): Flow<PagingData<TransactionWithRelations>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
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

    override suspend fun deleteCategory(category: Category, moveCategory: Category?) =
        db.withTransaction {
            val fromId = requireNotNull(category.id) { "deleteCategory: category.id is null" }

            val toId = moveCategory?.id
            if (toId != null) {
                transactionDao.moveTransactionsCategory(fromId, toId)
            }

            categoryDao.deleteCategory(category.toCategoryEntity())
        }


    override suspend fun deleteTag(deleteTag: Tag, moveTag: Tag?) = db.withTransaction {
        val fromId = requireNotNull(deleteTag.id) { "deleteTag: tag.id is null" }
        val toId = moveTag?.id

        if (toId != null) transactionDao.copyTagRefs(fromId, toId)
        transactionDao.deleteTagRefs(fromId)

        tagDao.deleteTag(deleteTag.toTagEntity())
    }


    override suspend fun deletePerson(deletePerson: Person, movePerson: Person?) =
        db.withTransaction {
            val fromId = requireNotNull(deletePerson.id) { "deletePerson: person.id is null" }
            val toId = movePerson?.id

            if (toId != null) transactionDao.copyPersonRefs(fromId, toId)
            transactionDao.deletePersonRefs(fromId)

            personDao.deletePerson(deletePerson.toPersonEntity())
        }


    override suspend fun deleteSource(deleteSource: Source, moveSource: Source?) =
        db.withTransaction {
            val fromId = requireNotNull(deleteSource.id) { "deleteSource: source.id is null" }
            val toId = moveSource?.id

            if (toId != null) {
                transactionDao.moveTransactionsSource(fromId, toId)
                transactionDao.moveTransactionsSourceEnd(fromId, toId)
            } else {
                transactionDao.nullifySourceEnd(fromId)
            }

            financialSourceDao.deleteSource(deleteSource.toSourceEntity())
        }


    override suspend fun insertFinancialSource(source: Source): Long {
        return financialSourceDao.insertFinancialSource(source.toSourceEntity())
    }

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(tag.toTagEntity())
    }

    override fun getAllCategory(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getAllCategories(type.count).map { it.map { it.toCategory() } }
    }

    override fun getAllFinancialSource(): Flow<List<Source>> {
        return financialSourceDao.getAllFinancialSources().map {
            it.map { it.toSource() }
        }
    }

    override fun getSource(sourceId: Long): Flow<Source?> {
        return financialSourceDao.getFinancialSourceById(sourceId).map { it?.toSource() }
    }

    override fun getAllTag(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { it.map { it.toTag() } }
    }

    override suspend fun adjustSourceBalance(id: Long, delta: Int) {
        financialSourceDao.adjustBalance(id, delta)
    }

    override suspend fun getDefaultCategory(type: TransactionType): Category {
        return categoryDao.getDefaultCategory(type.count).toCategory()
    }

    override suspend fun getTransferCategory(): Category = db.withTransaction {
        var transfer = categoryDao.getTransferCategory()?.toCategory()
        if (transfer == null) {
            categoryDao.createTransferCategory()
            transfer = categoryDao.getTransferCategory()?.toCategory()
        }
        transfer!!
    }


    override suspend fun getDefaultSource(): Source? {
        return financialSourceDao.getDefaultSource()?.toSource()
    }

    override suspend fun insertPerson(person: Person): Long {
        return personDao.insertPerson(person.toPersonEntity())
    }

    override fun getAllPersons(): Flow<List<Person>> {
        return personDao.getAllPersons().map { it.map { it.toPerson() } }
    }

    override suspend fun insertTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long = db.withTransaction {
        val id = insertTransaction(transaction, tagIds, personIds)

        balanceDeltas.forEach { (sourceId, delta) ->
            financialSourceDao.adjustBalance(sourceId, delta)
        }

        id
    }

    override suspend fun updateTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long = db.withTransaction {
        val row = update(transaction, tagIds, personIds) // همون متد فعلی خودت

        balanceDeltas.forEach { (sourceId, delta) ->
            financialSourceDao.adjustBalance(sourceId, delta)
        }

        row
    }

    override suspend fun deleteTransactionWithBalance(
        transaction: Transaction,
        balanceDeltas: Map<Long, Int>
    ) = db.withTransaction {
        transactionDao.deleteTransaction(transaction.toTransactionEntity())

        balanceDeltas.forEach { (sourceId, delta) ->
            financialSourceDao.adjustBalance(sourceId, delta)
        }
    }


}