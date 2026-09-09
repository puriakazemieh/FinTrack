package com.kazemieh.data.repository

import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import com.kazemieh.data_contract.datasource.ShoppingLocalDataSource
import com.kazemieh.domain.repository.ShoppingRepository
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.money.Currency
import kotlinx.coroutines.flow.Flow

class ShoppingRepositoryImpl(
    private val localDataSource: ShoppingLocalDataSource,
    private val preferenceRepository: PreferenceRepository
) : ShoppingRepository {
    override fun observeShoppingItems(
        categoryIds: List<Long>,
        tagIds: List<Long>
    ): Flow<List<ShoppingItem>> = if (categoryIds.isEmpty() && tagIds.isEmpty()) {
        localDataSource.observeShoppingItems()
    } else {
        localDataSource.observeShoppingItemsFiltered(categoryIds, tagIds)
    }

    override fun observeMostUsedTags(limit: Long): Flow<List<Tag>> =
        localDataSource.observeMostUsedTags(limit)

    override suspend fun getShoppingItemById(id: Long): ShoppingItem? = localDataSource.getShoppingItemById(id)
    override suspend fun addShoppingItem(item: ShoppingItem): Long =
        localDataSource.addShoppingItem(item.copy(currencyCode = selectedCurrencyCode()))
    override suspend fun updateShoppingItem(item: ShoppingItem) = localDataSource.updateShoppingItem(item)
    override suspend fun deleteShoppingItem(id: Long) = localDataSource.deleteShoppingItem(id)
    override suspend fun updatePositions(items: List<ShoppingItem>) = localDataSource.updatePositions(items)

    private fun selectedCurrencyCode(): String =
        Currency.valueOf(preferenceRepository.getString("PREF_CURRENCY", "IRT")).code
}
