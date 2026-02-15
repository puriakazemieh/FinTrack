package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category

class UpdateCategory(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(category : Category): Int {
        return repository.updateCategory(category)
    }
}
