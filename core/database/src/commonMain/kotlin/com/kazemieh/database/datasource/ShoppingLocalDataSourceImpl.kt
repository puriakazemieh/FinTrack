package com.kazemieh.database.datasource

import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import com.kazemieh.data_contract.datasource.ShoppingLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toShoppingItem
import com.kazemieh.database.mapper.toTag
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.database.ObserveShoppingItems
import com.kazemieh.database.ObserveShoppingItemsFiltered
import com.kazemieh.database.GetShoppingItemById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class ShoppingLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : ShoppingLocalDataSource {

    private val queries = db.shoppingQueries

    override fun observeShoppingItems(): Flow<List<ShoppingItem>> {
        return queries.observeShoppingItems()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toShoppingItem() } }
    }

    override fun observeShoppingItemsFiltered(
        categoryIds: List<Long>,
        tagIds: List<Long>
    ): Flow<List<ShoppingItem>> {
        return queries.observeShoppingItemsFiltered(
            categoryIds = categoryIds,
            categoryIdsSize = categoryIds.size.toLong(),
            tagIds = tagIds,
            tagIdsSize = tagIds.size.toLong()
        )
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toShoppingItem() } }
    }

    override suspend fun getShoppingItemById(id: Long): ShoppingItem? {
        return queries.getShoppingItemById(id).executeAsOneOrNull()?.toShoppingItem()
    }

    override suspend fun addShoppingItem(item: ShoppingItem): Long {
        return db.transactionWithResult {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.addShoppingItem(
                name = item.name,
                isChecked = item.isChecked,
                priority = item.priority.toLong(),
                estimatedPrice = item.estimatedPrice,
                reminderTime = item.reminderTime,
                categoryId = item.categoryId,
                note = item.note,
                position = item.position.toLong(),
                updatedAt = now,
                syncStatus = 1
            )
            val id = queries.lastInsertRowId().executeAsOne()
            item.tags.forEach { tag ->
                tag.id?.let { queries.insertShoppingItemTagCrossRef(id, it) }
            }
            id
        }
    }

    override fun observeMostUsedTags(limit: Long): Flow<List<Tag>> {
        return queries.getMostUsedShoppingTags(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toTag() } }
    }

    override suspend fun updateShoppingItem(item: ShoppingItem) {
        db.transaction {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateShoppingItem(
                name = item.name,
                isChecked = item.isChecked,
                priority = item.priority.toLong(),
                estimatedPrice = item.estimatedPrice,
                reminderTime = item.reminderTime,
                categoryId = item.categoryId,
                note = item.note,
                position = item.position.toLong(),
                updatedAt = now,
                syncStatus = 1,
                id = item.id
            )
            queries.deleteShoppingItemTagCrossRefs(item.id)
            item.tags.forEach { tag ->
                tag.id?.let { queries.insertShoppingItemTagCrossRef(item.id, it) }
            }
        }
    }

    override suspend fun deleteShoppingItem(id: Long) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.deleteShoppingItem(now, id)
    }

    override suspend fun updatePositions(items: List<ShoppingItem>) {
        db.transaction {
            val now = Clock.System.now().toEpochMilliseconds()
            items.forEach { item ->
                queries.updateShoppingItemPosition(item.position.toLong(), now, 1, item.id)
            }
        }
    }

    override suspend fun getAllShoppingItems(): List<ShoppingItem> = withContext(Dispatchers.Default) {
        queries.observeShoppingItems().executeAsList().map { it.toShoppingItem() }
    }

    override suspend fun insertFullShoppingItem(item: ShoppingItem) {
        withContext(Dispatchers.Default) {
            queries.insertFullShoppingItem(
                id = item.id,
                name = item.name,
                isChecked = item.isChecked,
                priority = item.priority.toLong(),
                estimatedPrice = item.estimatedPrice,
                reminderTime = item.reminderTime,
                categoryId = item.categoryId,
                note = item.note,
                position = item.position.toLong(),
                updatedAt = item.updatedAt,
                syncStatus = item.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getModifiedShoppingItems(): List<ShoppingItem> = withContext(Dispatchers.Default) {
        queries.getModifiedShoppingItems().executeAsList().map { it.toShoppingItem() }
    }

    override suspend fun markShoppingItemAsSynced(id: Long) {
        queries.markShoppingItemAsSynced(id)
    }

    override suspend fun physicallyDeleteShoppingItem(id: Long) {
        queries.markShoppingItemAsSynced(id)
    }
}
