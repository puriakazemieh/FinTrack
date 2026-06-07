package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Category
import com.kazemieh.domain.repository.TransactionRepository

class GetCategoryUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Category? {
        return repository.getCategoryById(id)
    }
}
