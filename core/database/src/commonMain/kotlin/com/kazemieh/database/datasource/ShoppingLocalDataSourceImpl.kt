package com.kazemieh.database.datasource

import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.data_contract.datasource.ShoppingLocalDataSource
import com.kazemieh.database.FinTrackDb
import com.kazemieh.database.mapper.toShoppingItem
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingLocalDataSourceImpl(
    private val db: FinTrackDb
) : ShoppingLocalDataSource {

    private val queries = db.shoppingQueries

    override fun observeShoppingItems(): Flow<List<ShoppingItem>> {
        return queries.observeShoppingItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toShoppingItem() } }
    }

    override suspend fun getShoppingItemById(id: Long): ShoppingItem? {
        return queries.getShoppingItemById(id).executeAsOneOrNull()?.toShoppingItem()
    }

    override suspend fun addShoppingItem(item: ShoppingItem): Long {
        return db.transactionWithResult {
            queries.addShoppingItem(
                name = item.name,
                isChecked = item.isChecked,
                priority = item.priority.toLong(),
                estimatedPrice = item.estimatedPrice,
                reminderTime = item.reminderTime,
                categoryId = item.categoryId,
                position = item.position.toLong()
            )
            queries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun updateShoppingItem(item: ShoppingItem) {
        queries.updateShoppingItem(
            name = item.name,
            isChecked = item.isChecked,
            priority = item.priority.toLong(),
            estimatedPrice = item.estimatedPrice,
            reminderTime = item.reminderTime,
            categoryId = item.categoryId,
            position = item.position.toLong(),
            id = item.id
        )
    }

    override suspend fun deleteShoppingItem(id: Long) {
        queries.deleteShoppingItem(id)
    }

    override suspend fun updatePositions(items: List<ShoppingItem>) {
        db.transaction {
            items.forEach { item ->
                queries.updateShoppingItemPosition(item.position.toLong(), item.id)
            }
        }
    }
}
