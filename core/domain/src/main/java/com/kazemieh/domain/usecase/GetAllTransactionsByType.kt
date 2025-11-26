package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

class GetAllTransactionsByType(
    private val repository: TransactionRepository
) {
    operator fun invoke(type: Int): Flow<List<TransactionWithRelations>> {
        return repository.getAllTransactionsByType(type)
    }
}
