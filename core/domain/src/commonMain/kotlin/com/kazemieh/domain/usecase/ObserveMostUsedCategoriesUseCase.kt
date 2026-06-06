package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveMostUsedCategoriesUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(type: TransactionType?, limit: Long = 5): Flow<List<Category>> {
        return repository.observeMostUsedCategories(type, limit)
    }
}
