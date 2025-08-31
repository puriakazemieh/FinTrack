package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class AddCategory(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(categoryName: String): Long {
        return repository.insertCategory(categoryName)
    }
}
