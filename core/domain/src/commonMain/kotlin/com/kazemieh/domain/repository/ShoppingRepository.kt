package com.kazemieh.domain.repository

import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * رابط کاربری برای مدیریت داده‌های لیست خرید.
 */
interface ShoppingRepository {
    fun observeShoppingItems(categoryIds: List<Long> = emptyList(), tagIds: List<Long> = emptyList()): Flow<List<ShoppingItem>>
    fun observeMostUsedTags(limit: Long): Flow<List<Tag>>
    suspend fun getShoppingItemById(id: Long): ShoppingItem?
    suspend fun addShoppingItem(item: ShoppingItem): Long
    suspend fun updateShoppingItem(item: ShoppingItem)
    suspend fun deleteShoppingItem(id: Long)
    suspend fun updatePositions(items: List<ShoppingItem>)
}
