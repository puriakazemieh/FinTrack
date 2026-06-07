package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Page
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toCategory
import com.kazemieh.database.mapper.toCategorySum
import com.kazemieh.database.mapper.toPerson
import com.kazemieh.database.mapper.toSource
import com.kazemieh.database.mapper.toTag
import com.kazemieh.database.mapper.toTransactionWithRelations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : TransactionLocalDataSource {

    private val transactionQueries = db.transactionQueries
    private val categoryQueries = db.categoryQueries
    private val sourceQueries = db.sourceQueries
    private val tagQueries = db.tagQueries
    private val personQueries = db.personQueries
    private val transactionTagQueries = db.transactionTagQueries
    private val transactionPersonQueries = db.transactionPersonQueries


    override fun observeTransactions(
        transactionFilterParams: TransactionFilterParams,
        request: PageRequest
    ): Flow<Page<TransactionWithRelations>> {
        val categoryIds = transactionFilterParams.categories.mapNotNull { it.id }
        val sourceIds = transactionFilterParams.sources.mapNotNull { it.id }
        val tagIds = transactionFilterParams.tags.mapNotNull { it.id }
        val personIds = transactionFilterParams.persons.mapNotNull { it.id }

        return transactionQueries.getAllTransactionsFiltered(
            type = transactionFilterParams.type?.toLong(),
            categoryIds = categoryIds,
            categoryIdsSize = if (transactionFilterParams.isAllCategories) 0 else categoryIds.size.toLong().coerceAtLeast(1),
            sourceIds = sourceIds,
            sourceIdsSize = if (transactionFilterParams.isAllSources) 0 else sourceIds.size.toLong().coerceAtLeast(1),
            tagIds = tagIds,
            tagIdsSize = if (transactionFilterParams.isAllTags) 0 else tagIds.size.toLong().coerceAtLeast(1),
            personIds = personIds,
            personIdsSize = if (transactionFilterParams.isAllPersons) 0 else personIds.size.toLong().coerceAtLeast(1),
            fromTimestamp = transactionFilterParams.fromTimestamp,
            toTimestamp = transactionFilterParams.toTimestamp,
            limit = request.limit.toLong(),
            offset = request.offset.toLong()
        )
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { results ->
                Page(
                    items = results.map { it.toTransactionWithRelations() },
                    request = request
                )
            }
    }

    override fun observeCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> {
        val categoryIds = transactionFilterParams.categories.mapNotNull { it.id }
        val sourceIds = transactionFilterParams.sources.mapNotNull { it.id }
        val tagIds = transactionFilterParams.tags.mapNotNull { it.id }
        val personIds = transactionFilterParams.persons.mapNotNull { it.id }

        return transactionQueries.observeCategorySumsByFilter(
            type = transactionFilterParams.type?.toLong(),
            categoryIds = categoryIds,
            categoryIdsSize = if (transactionFilterParams.isAllCategories) 0 else categoryIds.size.toLong().coerceAtLeast(1),
            sourceIds = sourceIds,
            sourceIdsSize = if (transactionFilterParams.isAllSources) 0 else sourceIds.size.toLong().coerceAtLeast(1),
            tagIds = tagIds,
            tagIdsSize = if (transactionFilterParams.isAllTags) 0 else tagIds.size.toLong().coerceAtLeast(1),
            personIds = personIds,
            personIdsSize = if (transactionFilterParams.isAllPersons) 0 else personIds.size.toLong().coerceAtLeast(1),
            fromTimestamp = transactionFilterParams.fromTimestamp,
            toTimestamp = transactionFilterParams.toTimestamp
        )
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { row -> row.toCategorySum() } }
    }

    override suspend fun addCategory(category: Category): Long = withContext(Dispatchers.Default) {
        categoryQueries.addCategory(
            name = category.name,
            description = category.description,
            type = category.type.count.toLong(),
            colorId = category.colorId.toLong(),
            iconId = category.iconId.toLong(),
            position = category.position.toLong()
        )
        categoryQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun updateCategory(category: Category): Int =
        withContext(Dispatchers.Default) {
            categoryQueries.updateCategory(
                name = category.name,
                description = category.description,
                type = category.type.count.toLong(),
                colorId = category.colorId.toLong(),
                iconId = category.iconId.toLong(),
                position = category.position.toLong(),
                id = category.id ?: 0
            )

            // بررسی کن که واقعاً update شده
            val updated = categoryQueries.getCategoryById(category.id ?: 0)
                .awaitAsOneOrNull()

            if (updated == null) {
                throw IllegalStateException("Category update failed")
            }

            1
        }

    override suspend fun updateSource(source: Source): Int = withContext(Dispatchers.Default) {
        val id = source.id ?: throw IllegalArgumentException("Source ID cannot be null")

        sourceQueries.updateSource(
            name = source.name,
            balance = source.balance.toLong(),
            cardNumber = source.cardNumber,
            description = source.description,
            type = source.type.toLong(),
            colorId = source.colorId.toLong(),
            iconId = source.iconId.toLong(),
            shabaNumber = source.shabaNumber,
            accountNumber = source.accountNumber,
            cvv2 = source.cvv2,
            expirationMonth = source.expirationMonth,
            expirationYear = source.expirationYear,
            branchCode = source.branchCode,
            branchName = source.branchName,
            position = source.position.toLong(),
            id = id
        )

        // بررسی موفقیت
        val updated = sourceQueries.getSourceById(id).awaitAsOneOrNull()
        if (updated == null) {
            throw IllegalStateException("Source update failed")
        }

        1
    }

    override suspend fun updateTag(tag: Tag): Int = withContext(Dispatchers.Default) {
        tagQueries.updateTag(
            name = tag.name,
            description = tag.description,
            colorId = tag.colorId.toLong(),
            iconId = tag.iconId.toLong(),
            position = tag.position.toLong(),
            id = tag.id ?: 0
        )
        1
    }

    override suspend fun updatePerson(person: Person): Int = withContext(Dispatchers.Default) {
        personQueries.updatePerson(
            name = person.name,
            description = person.description,
            position = person.position.toLong(),
            id = person.id ?: 0
        )
        1
    }

    override suspend fun deleteCategory(category: Category, moveCategory: Category?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(category.id)
                val toId = moveCategory?.id

                if (toId != null) {
                    transactionQueries.moveTransactionsCategory(
                        categoryId = toId,
                        categoryId_ = fromId
                    )
                }

                // اگر کتگوری انتقال وجود نداشت، بساز
                categoryQueries.insertTransferCategoryIfMissing()

                // حالا کتگوری رو پاک کن
                categoryQueries.deleteCategory(fromId)
            }
        }

    override suspend fun deleteTag(deleteTag: Tag, moveTag: Tag?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(deleteTag.id)
                val toId = moveTag?.id

                if (toId != null) {
                    transactionTagQueries.copyTagRefs(fromTagId = fromId, toTagId = toId)
                }

                transactionTagQueries.deleteTagRefs(fromId)
                tagQueries.deleteTag(fromId)
            }
        }

    override suspend fun deletePerson(deletePerson: Person, movePerson: Person?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(deletePerson.id)
                val toId = movePerson?.id

                if (toId != null) {
                    transactionPersonQueries.copyPersonRefs(
                        fromPersonId = fromId,
                        toPersonId = toId
                    )
                }

                transactionPersonQueries.deletePersonRefs(fromId)
                personQueries.deletePerson(fromId)
            }
        }

    override suspend fun deleteSource(deleteSource: Source, moveSource: Source?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(deleteSource.id)
                val toId = moveSource?.id

                if (toId != null) {
                    transactionQueries.moveTransactionsSource(sourceId = toId, sourceId_ = fromId)
                    transactionQueries.moveTransactionsSourceEnd(
                        sourceEndId = toId,
                        sourceEndId_ = fromId
                    )
                } else {
                    transactionQueries.nullifySourceEnd(fromId)
                }

                sourceQueries.deleteSource(fromId)
            }
        }

    override suspend fun addSource(source: Source): Long = withContext(Dispatchers.Default) {
        sourceQueries.addSource(
            name = source.name,
            balance = source.balance.toLong(),
            cardNumber = source.cardNumber,
            description = source.description,
            type = source.type.toLong(),
            colorId = source.colorId.toLong(),
            iconId = source.iconId.toLong(),
            shabaNumber = source.shabaNumber,
            accountNumber = source.accountNumber,
            cvv2 = source.cvv2,
            expirationMonth = source.expirationMonth,
            expirationYear = source.expirationYear,
            branchCode = source.branchCode,
            branchName = source.branchName,
            position = source.position.toLong()
        )
        sourceQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun addTag(tag: Tag): Long = withContext(Dispatchers.Default) {
        tagQueries.addTag(
            name = tag.name,
            description = tag.description,
            colorId = tag.colorId.toLong(),
            iconId = tag.iconId.toLong(),
            position = tag.position.toLong()
        )
        tagQueries.lastInsertRowId().awaitAsOne()
    }

    override fun observeCategories(type: TransactionType?): Flow<List<Category>> {
        val count = if (type == TransactionType.ALL) null else type?.count?.toLong()
        return categoryQueries.observeCategories(count)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toCategory() } }
    }

    override suspend fun getCategoryById(id: Long): Category? = withContext(Dispatchers.Default) {
        categoryQueries.getCategoryById(id)
            .awaitAsOneOrNull()
            ?.toCategory()
    }

    override fun observeSources(): Flow<List<Source>> {
        return sourceQueries.observeSources()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { s -> s.toSource() } }
    }

    override fun observeSource(sourceId: Long): Flow<Source?> {
        return sourceQueries.observeSourceById(sourceId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toSource() }
    }

    override fun observeTags(): Flow<List<Tag>> {
        return tagQueries.observeTags()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toTag() } }
    }

    override suspend fun adjustSourceBalance(id: Long, delta: Int): Unit =
        withContext(Dispatchers.Default) {
            sourceQueries.adjustBalance(delta.toLong(), id)
        }

    override suspend fun getDefaultCategory(type: TransactionType): Category =
        withContext(Dispatchers.Default) {
            categoryQueries.getFirstByType(type.count.toLong())
                .awaitAsOne()
                .toCategory()
        }

    override suspend fun getTransferCategory(): Category =
        withContext(Dispatchers.Default) {
            db.transactionWithResult {
                var transfer = categoryQueries.getTransferCategoryOrNull()
                    .awaitAsOneOrNull()
                    ?.toCategory()

                if (transfer == null) {
                    categoryQueries.createTransferCategory()
                    transfer = categoryQueries.getTransferCategoryOrNull()
                        .awaitAsOne()
                        .toCategory()
                }

                transfer
            }
        }

    override suspend fun getDefaultSource(): Source? =
        withContext(Dispatchers.Default) {
            sourceQueries.getDefaultSource()
                .awaitAsOneOrNull()
                ?.toSource()
        }

    override suspend fun addPerson(person: Person): Long =
        withContext(Dispatchers.Default) {
            personQueries.addPerson(
                name = person.name,
                description = person.description,
                position = person.position.toLong()
            )
            personQueries.lastInsertRowId().awaitAsOne()
        }

    override fun observePersons(): Flow<List<Person>> {
        return personQueries.observePersons()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { p -> p.toPerson() } }
    }

    override fun observeMostUsedCategories(type: TransactionType?, limit: Long): Flow<List<Category>> {
        return categoryQueries.getMostUsedCategories(type?.count?.toLong(), limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { c -> c.toCategory() } }
    }

    override fun observeMostUsedSources(limit: Long): Flow<List<Source>> {
        return sourceQueries.getMostUsedSources(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { s -> s.toSource() } }
    }

    override fun observeMostUsedTags(limit: Long): Flow<List<Tag>> {
        return tagQueries.getMostUsedTags(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toTag() } }
    }

    override fun observeMostUsedPersons(limit: Long): Flow<List<Person>> {
        return personQueries.getMostUsedPersons(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { p -> p.toPerson() } }
    }

    override suspend fun updateCategoryPosition(id: Long, position: Int) {
        categoryQueries.updateCategoryPosition(position.toLong(), id)
    }

    override suspend fun updateSourcePosition(id: Long, position: Int) {
        sourceQueries.updateSourcePosition(position.toLong(), id)
    }

    override suspend fun updateTagPosition(id: Long, position: Int) {
        tagQueries.updateTagPosition(position.toLong(), id)
    }

    override suspend fun updatePersonPosition(id: Long, position: Int) {
        personQueries.updatePersonPosition(position.toLong(), id)
    }

    override suspend fun addTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long = withContext(Dispatchers.Default) {
        db.transactionWithResult {

            transactionQueries.insertTransaction(
                amount = transaction.amount.toLong(),
                amountTransfer = transaction.amountTransfer.toLong(),
                categoryId = transaction.categoryId,
                sourceId = transaction.sourceId,
                sourceEndId = transaction.sourceEndId,
                description = transaction.description,
                timeStamp = transaction.timeStamp,
                type = transaction.type.count.toLong()
            )

            val transactionId = transactionQueries.lastInsertRowId().awaitAsOne()

            tagIds.distinct().forEach { tagId ->
                transactionTagQueries.insertTransactionTagCrossRefs(transactionId, tagId)
            }

            personIds.distinct().forEach { personId ->
                transactionPersonQueries.insertTransactionPersonCrossRefs(transactionId, personId)
            }


            balanceDeltas.forEach { (sourceId, delta) ->
                sourceQueries.adjustBalance(delta.toLong(), sourceId)
            }

            transactionId
        }
    }

    override suspend fun updateTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long = withContext(Dispatchers.Default) {
        db.transactionWithResult {

            transactionQueries.updateTransaction(
                amount = transaction.amount.toLong(),
                amountTransfer = transaction.amountTransfer.toLong(),
                categoryId = transaction.categoryId,
                sourceId = transaction.sourceId,
                sourceEndId = transaction.sourceEndId,
                description = transaction.description,
                timeStamp = transaction.timeStamp,
                type = transaction.type.count.toLong(),
                id = transaction.id
            )

            val exists = transactionQueries.getTransactionById(transaction.id)
                .awaitAsOneOrNull() != null

            if (!exists) {
                throw IllegalStateException("Transaction update failed: record not found")
            }

            transactionTagQueries.deleteAllTagRefsForTransaction(transaction.id)
            transactionPersonQueries.deleteAllPersonRefsForTransaction(transaction.id)

            tagIds.distinct().forEach { tagId ->
                transactionTagQueries.insertTransactionTagCrossRefs(transaction.id, tagId)
            }

            personIds.distinct().forEach { personId ->
                transactionPersonQueries.insertTransactionPersonCrossRefs(transaction.id, personId)
            }

            transaction.id

        }
    }

    override suspend fun deleteTransactionWithBalance(
        transaction: Transaction,
        balanceDeltas: Map<Long, Int>
    ) = withContext(Dispatchers.Default) {
        db.transaction {
            transactionQueries.deleteTransaction(transaction.id)

            balanceDeltas.forEach { (sourceId, delta) ->
                sourceQueries.adjustBalance(delta.toLong(), sourceId)
            }
        }
    }
}
