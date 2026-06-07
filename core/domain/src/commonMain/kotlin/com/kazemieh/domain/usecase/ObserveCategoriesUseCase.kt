package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(type: TransactionType? = null): Flow<List<Category>> {
        return repository.observeCategories(type)
    }
}
