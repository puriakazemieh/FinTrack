package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category

class AddCategory(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(category : Category): Long {
        return repository.insertCategory(category)
    }
}
