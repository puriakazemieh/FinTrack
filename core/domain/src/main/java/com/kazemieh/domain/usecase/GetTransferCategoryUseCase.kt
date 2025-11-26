package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category

class GetTransferCategoryUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Category {
        return repository.getTransferCategory()
    }
}