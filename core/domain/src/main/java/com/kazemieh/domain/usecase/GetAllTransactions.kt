package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

class GetAllTransactions(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<TransactionWithRelations>> {
        return repository.getAllTransactions()
    }
}
