package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Category

class DeleteCategory(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(deleteCategory: Category, moveCategory: Category?) {
        return repository.deleteCategory(deleteCategory, moveCategory)
    }
}
