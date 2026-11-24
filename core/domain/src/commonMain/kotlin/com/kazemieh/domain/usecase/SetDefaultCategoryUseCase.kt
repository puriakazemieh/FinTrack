package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class SetDefaultCategoryUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: Long): Boolean = repository.setDefaultCategory(id)
}
