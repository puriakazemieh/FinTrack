package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category
import kotlinx.coroutines.flow.Flow

class GetAllCategoryByType(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(type: Int): Flow<List<Category>> {
        return repository.getAllCategory(type)
    }
}
