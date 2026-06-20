package com.kazemieh.domain.repository

import com.kazemieh.common.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

/**
 * رابط کاربری برای مدیریت داده‌های لیست خرید.
 */
interface ShoppingRepository {
    fun observeShoppingItems(): Flow<List<ShoppingItem>>
    suspend fun getShoppingItemById(id: Long): ShoppingItem?
    suspend fun addShoppingItem(item: ShoppingItem): Long
    suspend fun updateShoppingItem(item: ShoppingItem)
    suspend fun deleteShoppingItem(id: Long)
    suspend fun updatePositions(items: List<ShoppingItem>)
}
