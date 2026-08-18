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
import com.kazemieh.database.mapper.toTransaction
import com.kazemieh.database.mapper.toTransactionWithRelations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

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
            minAmount = transactionFilterParams.minAmount?.toLong(),
            maxAmount = transactionFilterParams.maxAmount?.toLong(),
            query = transactionFilterParams.query,
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
            toTimestamp = transactionFilterParams.toTimestamp,
            minAmount = transactionFilterParams.minAmount?.toLong(),
            maxAmount = transactionFilterParams.maxAmount?.toLong(),
            query = transactionFilterParams.query
        )
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { row -> row.toCategorySum() } }
    }

    override suspend fun addCategory(category: Category): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        categoryQueries.addCategory(
            name = category.name,
            description = category.description,
            type = category.type.count.toLong(),
            colorId = category.colorId.toLong(),
            iconId = category.iconId.toLong(),
            position = category.position.toLong(),
            parentId = category.parentId,
            updatedAt = now,
            syncStatus = 1
        )
        categoryQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun updateCategory(category: Category): Int =
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            categoryQueries.updateCategory(
                name = category.name,
                description = category.description,
                type = category.type.count.toLong(),
                colorId = category.colorId.toLong(),
                iconId = category.iconId.toLong(),
                position = category.position.toLong(),
                parentId = category.parentId,
                updatedAt = now,
                syncStatus = 1,
                id = category.id ?: 0
            )
            1
        }

    override suspend fun updateSource(source: Source): Int = withContext(Dispatchers.Default) {
        val id = source.id ?: throw IllegalArgumentException("Source ID cannot be null")
        val now = Clock.System.now().toEpochMilliseconds()

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
            updatedAt = now,
            syncStatus = 1,
            id = id
        )
        1
    }

    override suspend fun updateTag(tag: Tag): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        tagQueries.updateTag(
            name = tag.name,
            description = tag.description,
            colorId = tag.colorId.toLong(),
            iconId = tag.iconId.toLong(),
            position = tag.position.toLong(),
            updatedAt = now,
            syncStatus = 1,
            id = tag.id ?: 0
        )
        1
    }

    override suspend fun updatePerson(person: Person): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        personQueries.updatePerson(
            name = person.name,
            description = person.description,
            position = person.position.toLong(),
            updatedAt = now,
            syncStatus = 1,
            id = person.id ?: 0
        )
        1
    }

    override suspend fun deleteCategory(category: Category, moveCategory: Category?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(category.id)
                val toId = moveCategory?.id
                val now = Clock.System.now().toEpochMilliseconds()

                if (toId != null) {
                    transactionQueries.moveTransactionsCategory(
                        categoryId = toId,
                        updatedAt = now,
                        syncStatus = 1,
                        categoryId_ = fromId
                    )
                }

                categoryQueries.insertTransferCategoryIfMissing(now)

                categoryQueries.deleteCategory(now, fromId)
            }
        }

    override suspend fun deleteTag(deleteTag: Tag, moveTag: Tag?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(deleteTag.id)
                val toId = moveTag?.id
                val now = Clock.System.now().toEpochMilliseconds()

                if (toId != null) {
                    transactionTagQueries.copyTagRefs(fromTagId = fromId, toTagId = toId)
                }

                transactionTagQueries.deleteTagRefs(fromId)
                tagQueries.deleteTag(now, fromId)
            }
        }

    override suspend fun deletePerson(deletePerson: Person, movePerson: Person?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(deletePerson.id)
                val toId = movePerson?.id
                val now = Clock.System.now().toEpochMilliseconds()

                if (toId != null) {
                    transactionPersonQueries.copyPersonRefs(
                        fromPersonId = fromId,
                        toPersonId = toId
                    )
                }

                transactionPersonQueries.deletePersonRefs(fromId)
                personQueries.deletePerson(now, fromId)
            }
        }

    override suspend fun deleteSource(deleteSource: Source, moveSource: Source?) =
        withContext(Dispatchers.Default) {
            db.transaction {
                val fromId = requireNotNull(deleteSource.id)
                val toId = moveSource?.id
                val now = Clock.System.now().toEpochMilliseconds()

                if (toId != null) {
                    transactionQueries.moveTransactionsSource(sourceId = toId, updatedAt = now, syncStatus = 1, sourceId_ = fromId)
                    transactionQueries.moveTransactionsSourceEnd(
                        sourceEndId = toId,
                        updatedAt = now,
                        syncStatus = 1,
                        sourceEndId_ = fromId
                    )
                } else {
                    transactionQueries.nullifySourceEnd(now, 1, fromId)
                }

                sourceQueries.deleteSource(now, fromId)
            }
        }

    override suspend fun addSource(source: Source): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
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
            position = source.position.toLong(),
            updatedAt = now,
            syncStatus = 1
        )
        sourceQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun addTag(tag: Tag): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        tagQueries.addTag(
            name = tag.name,
            description = tag.description,
            colorId = tag.colorId.toLong(),
            iconId = tag.iconId.toLong(),
            position = tag.position.toLong(),
            updatedAt = now,
            syncStatus = 1
        )
        tagQueries.lastInsertRowId().awaitAsOne()
    }

    override fun observeCategories(type: TransactionType?, parentId: Long?): Flow<List<Category>> {
        val count = if (type == TransactionType.ALL) null else type?.count?.toLong()
        return categoryQueries.observeCategories(count, parentId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toCategory() } }
    }

    override fun observeCategoriesFlat(type: TransactionType?): Flow<List<Category>> {
        val count = if (type == TransactionType.ALL) null else type?.count?.toLong()
        return categoryQueries.observeCategoriesFlat(count)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toCategory() } }
    }

    override suspend fun getCategoryById(id: Long): Category? = withContext(Dispatchers.Default) {
        categoryQueries.getCategoryById(id)
            .awaitAsOneOrNull()
            ?.toCategory()
    }

    override suspend fun getPersonById(id: Long): Person? = withContext(Dispatchers.Default) {
        personQueries.getPersonById(id)
            .awaitAsOneOrNull()
            ?.toPerson()
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

    override suspend fun getSourceById(id: Long): Source? = withContext(Dispatchers.Default) {
        sourceQueries.getSourceById(id)
            .awaitAsOneOrNull()
            ?.toSource()
    }

    override fun observeTags(): Flow<List<Tag>> {
        return tagQueries.observeTags()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toTag() } }
    }

    override suspend fun getTagById(id: Long): Tag? = withContext(Dispatchers.Default) {
        tagQueries.getTagById(id)
            .awaitAsOneOrNull()
            ?.toTag()
    }

    override suspend fun adjustSourceBalance(id: Long, delta: Int): Unit =
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            sourceQueries.adjustBalance(delta.toLong(), now, 1, id)
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
                val now = Clock.System.now().toEpochMilliseconds()
                var transfer = categoryQueries.getTransferCategoryOrNull()
                    .awaitAsOneOrNull()
                    ?.toCategory()

                if (transfer == null) {
                    categoryQueries.createTransferCategory(now)
                    transfer = categoryQueries.getTransferCategoryOrNull()
                        .awaitAsOne()
                        .toCategory()
                }

                transfer
            }
        }

    override suspend fun getSourceByIdentifier(identifier: String): Source? = withContext(Dispatchers.Default) {
        sourceQueries.getSourceByIdentifier(identifier)
            .awaitAsOneOrNull()
            ?.toSource()
    }

    override suspend fun getDefaultSource(): Source? =
        withContext(Dispatchers.Default) {
            sourceQueries.getDefaultSource()
                .awaitAsOneOrNull()
                ?.toSource()
        }

    override suspend fun addPerson(person: Person): Long =
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            personQueries.addPerson(
                name = person.name,
                description = person.description,
                position = person.position.toLong(),
                updatedAt = now,
                syncStatus = 1
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
            .map { list -> list.map { it.toCategory() } }
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

    override fun searchCategories(query: String): Flow<List<Category>> {
        return categoryQueries.searchCategories(query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { c -> c.toCategory() } }
    }

    override fun searchSources(query: String): Flow<List<Source>> {
        return sourceQueries.searchSources(query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { s -> s.toSource() } }
    }

    override fun searchPersons(query: String): Flow<List<Person>> {
        return personQueries.searchPersons(query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { p -> p.toPerson() } }
    }

    override fun searchTags(query: String): Flow<List<Tag>> {
        return tagQueries.searchTags(query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toTag() } }
    }

    override suspend fun updateCategoryPosition(id: Long, position: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        categoryQueries.updateCategoryPosition(position.toLong(), now, 1, id)
    }

    override suspend fun updateSourcePosition(id: Long, position: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        sourceQueries.updateSourcePosition(position.toLong(), now, 1, id)
    }

    override suspend fun updateTagPosition(id: Long, position: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        tagQueries.updateTagPosition(position.toLong(), now, 1, id)
    }

    override suspend fun updatePersonPosition(id: Long, position: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        personQueries.updatePersonPosition(position.toLong(), now, 1, id)
    }

    override suspend fun getTransactionAmountRange(): Pair<Long, Long> = withContext(Dispatchers.Default) {
        val row = transactionQueries.getTransactionAmountRange().awaitAsOne()
        Pair(row.MIN ?: 0L, row.MAX ?: 1_000_000L)
    }

    override suspend fun getTransactionCount(): Long = withContext(Dispatchers.Default) {
        transactionQueries.getTransactionCount().awaitAsOne()
    }

    override suspend fun getAllTransactions(): List<Transaction> = withContext(Dispatchers.Default) {
        transactionQueries.getAllTransactions()
            .awaitAsList()
            .map { it.toTransaction() }
    }

    override suspend fun insertFullTransaction(transaction: Transaction) {
        withContext(Dispatchers.Default) {
            transactionQueries.insertFullTransaction(
                id = transaction.id,
                amount = transaction.amount.toLong(),
                amountTransfer = transaction.amountTransfer.toLong(),
                categoryId = transaction.categoryId,
                sourceId = transaction.sourceId,
                sourceEndId = transaction.sourceEndId,
                relatedDebtId = transaction.relatedDebtId,
                description = transaction.description,
                photoPath = transaction.photoPath,
                timeStamp = transaction.timeStamp,
                type = transaction.type.count.toLong(),
                updatedAt = transaction.updatedAt,
                syncStatus = transaction.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.Default) {
        categoryQueries.observeCategoriesFlat(null).awaitAsList().map { it.toCategory() }
    }

    override suspend fun insertFullCategory(category: Category) {
        withContext(Dispatchers.Default) {
            categoryQueries.insertFullCategory(
                id = category.id ?: 0,
                name = category.name,
                description = category.description,
                type = category.type.count.toLong(),
                colorId = category.colorId.toLong(),
                iconId = category.iconId.toLong(),
                position = category.position.toLong(),
                parentId = category.parentId,
                updatedAt = category.updatedAt,
                syncStatus = category.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getAllSources(): List<Source> = withContext(Dispatchers.Default) {
        sourceQueries.observeSources().awaitAsList().map { it.toSource() }
    }

    override suspend fun insertFullSource(source: Source) {
        withContext(Dispatchers.Default) {
            sourceQueries.insertFullSource(
                id = source.id ?: 0,
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
                updatedAt = source.updatedAt,
                syncStatus = source.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getAllTags(): List<Tag> = withContext(Dispatchers.Default) {
        tagQueries.observeTags().awaitAsList().map { it.toTag() }
    }

    override suspend fun insertFullTag(tag: Tag) {
        withContext(Dispatchers.Default) {
            tagQueries.insertFullTag(
                id = tag.id ?: 0,
                name = tag.name,
                description = tag.description,
                colorId = tag.colorId.toLong(),
                iconId = tag.iconId.toLong(),
                position = tag.position.toLong(),
                updatedAt = tag.updatedAt,
                syncStatus = tag.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getAllPersons(): List<Person> = withContext(Dispatchers.Default) {
        personQueries.observePersons().awaitAsList().map { it.toPerson() }
    }

    override suspend fun insertFullPerson(person: Person) {
        withContext(Dispatchers.Default) {
            personQueries.insertFullPerson(
                id = person.id ?: 0,
                name = person.name,
                description = person.description,
                position = person.position.toLong(),
                updatedAt = person.updatedAt,
                syncStatus = person.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun addTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Long>
    ): Long = withContext(Dispatchers.Default) {
        db.transactionWithResult {
            val now = Clock.System.now().toEpochMilliseconds()
            transactionQueries.insertTransaction(
                amount = transaction.amount.toLong(),
                amountTransfer = transaction.amountTransfer.toLong(),
                categoryId = transaction.categoryId,
                sourceId = transaction.sourceId,
                sourceEndId = transaction.sourceEndId,
                relatedDebtId = transaction.relatedDebtId,
                description = transaction.description,
                photoPath = transaction.photoPath,
                timeStamp = transaction.timeStamp,
                type = transaction.type.count.toLong(),
                updatedAt = now,
                syncStatus = 1
            )

            val transactionId = transactionQueries.lastInsertRowId().awaitAsOne()

            tagIds.distinct().forEach { tagId ->
                transactionTagQueries.insertTransactionTagCrossRefs(transactionId, tagId)
            }

            personIds.distinct().forEach { personId ->
                transactionPersonQueries.insertTransactionPersonCrossRefs(transactionId, personId)
            }


            balanceDeltas.forEach { (sourceId, delta) ->
                sourceQueries.adjustBalance(delta.toLong(), now, 1, sourceId)
            }

            transactionId
        }
    }

    override suspend fun updateTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Long>
    ): Long = withContext(Dispatchers.Default) {
        db.transactionWithResult {
            val now = Clock.System.now().toEpochMilliseconds()
            transactionQueries.updateTransaction(
                amount = transaction.amount.toLong(),
                amountTransfer = transaction.amountTransfer.toLong(),
                categoryId = transaction.categoryId,
                sourceId = transaction.sourceId,
                sourceEndId = transaction.sourceEndId,
                relatedDebtId = transaction.relatedDebtId,
                description = transaction.description,
                photoPath = transaction.photoPath,
                timeStamp = transaction.timeStamp,
                type = transaction.type.count.toLong(),
                updatedAt = now,
                syncStatus = 1,
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

            balanceDeltas.forEach { (sourceId, delta) ->
                sourceQueries.adjustBalance(delta.toLong(), now, 1, sourceId)
            }

            transaction.id

        }
    }

    override suspend fun deleteTransactionWithBalance(
        transaction: Transaction,
        balanceDeltas: Map<Long, Long>
    ) = withContext(Dispatchers.Default) {
        db.transaction {
            val now = Clock.System.now().toEpochMilliseconds()
            transactionQueries.deleteTransaction(now, transaction.id)

            balanceDeltas.forEach { (sourceId, delta) ->
                sourceQueries.adjustBalance(delta.toLong(), now, 1, sourceId)
            }
        }
    }

    override suspend fun getModifiedTransactions(): List<Transaction> = withContext(Dispatchers.Default) {
        transactionQueries.getModifiedTransactions().awaitAsList().map { it.toTransaction() }
    }

    override suspend fun markTransactionAsSynced(id: Long) {
        transactionQueries.markTransactionAsSynced(id)
    }

    override suspend fun getModifiedCategories(): List<Category> = withContext(Dispatchers.Default) {
        categoryQueries.getModifiedCategories().awaitAsList().map { it.toCategory() }
    }

    override suspend fun markCategoryAsSynced(id: Long) {
        categoryQueries.markCategoryAsSynced(id)
    }

    override suspend fun getModifiedSources(): List<Source> = withContext(Dispatchers.Default) {
        sourceQueries.getModifiedSources().awaitAsList().map { it.toSource() }
    }

    override suspend fun markSourceAsSynced(id: Long) {
        sourceQueries.markSourceAsSynced(id)
    }

    override suspend fun getModifiedTags(): List<Tag> = withContext(Dispatchers.Default) {
        tagQueries.getModifiedTags().awaitAsList().map { it.toTag() }
    }

    override suspend fun markTagAsSynced(id: Long) {
        tagQueries.markTagAsSynced(id)
    }

    override suspend fun getModifiedPersons(): List<Person> = withContext(Dispatchers.Default) {
        personQueries.getModifiedPersons().awaitAsList().map { it.toPerson() }
    }

    override suspend fun markPersonAsSynced(id: Long) {
        personQueries.markPersonAsSynced(id)
    }

    override suspend fun physicallyDeleteTransaction(id: Long) {
        transactionQueries.physicallyDeleteTransaction(id)
    }

    override suspend fun physicallyDeleteCategory(id: Long) {
        categoryQueries.physicallyDeleteCategory(id)
    }

    override suspend fun physicallyDeleteSource(id: Long) {
        sourceQueries.physicallyDeleteSource(id)
    }

    override suspend fun physicallyDeleteTag(id: Long) {
        tagQueries.physicallyDeleteTag(id)
    }

    override suspend fun physicallyDeletePerson(id: Long) {
        personQueries.physicallyDeletePerson(id)
    }
}
