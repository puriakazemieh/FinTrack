package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType

class GetDefaultCategoryUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactionType: TransactionType): Category {
        return repository.getDefaultCategory(transactionType)
    }
}