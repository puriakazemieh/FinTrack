package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingLocalDataSource {
    fun observeShoppingItems(): Flow<List<ShoppingItem>>
    suspend fun getShoppingItemById(id: Long): ShoppingItem?
    suspend fun addShoppingItem(item: ShoppingItem): Long
    suspend fun updateShoppingItem(item: ShoppingItem)
    suspend fun deleteShoppingItem(id: Long)
    suspend fun updatePositions(items: List<ShoppingItem>)

    suspend fun getAllShoppingItems(): List<ShoppingItem>
    suspend fun insertFullShoppingItem(item: ShoppingItem)
    suspend fun getModifiedShoppingItems(): List<ShoppingItem>
    suspend fun markShoppingItemAsSynced(id: Long)
    suspend fun physicallyDeleteShoppingItem(id: Long)
}
