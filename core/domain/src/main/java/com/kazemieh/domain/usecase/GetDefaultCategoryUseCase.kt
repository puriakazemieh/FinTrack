package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Category

class GetDefaultCategoryUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactionType: Int): Category {
        return repository.getDefaultCategory(transactionType)
    }
}