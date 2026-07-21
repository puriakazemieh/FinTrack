package com.kazemieh.domain.usecase

import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow

class ObserveShoppingItemsUseCase(private val repository: ShoppingRepository) {
    operator fun invoke(categoryIds: List<Long> = emptyList(), tagIds: List<Long> = emptyList()): Flow<List<ShoppingItem>> =
        repository.observeShoppingItems(categoryIds, tagIds)
}

class AddShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(item: ShoppingItem): Long = repository.addShoppingItem(item)
}

class UpdateShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(item: ShoppingItem) = repository.updateShoppingItem(item)
}

class DeleteShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteShoppingItem(id)
}

class UpdateShoppingPositionsUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(items: List<ShoppingItem>) = repository.updatePositions(items)
}

class ObserveMostUsedShoppingTagsUseCase(private val repository: ShoppingRepository) {
    operator fun invoke(limit: Long = 5): Flow<List<Tag>> = repository.observeMostUsedTags(limit)
}
