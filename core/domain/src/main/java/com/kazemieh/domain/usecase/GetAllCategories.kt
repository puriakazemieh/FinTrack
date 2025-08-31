package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Category
import kotlinx.coroutines.flow.Flow

class GetAllCategory(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Flow<List<Category>> {
        return repository.getAllCategory()
    }
}
