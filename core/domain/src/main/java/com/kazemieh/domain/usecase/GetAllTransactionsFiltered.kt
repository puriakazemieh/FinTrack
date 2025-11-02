package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

class GetAllTransactionsFiltered(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<List<TransactionWithRelations>> {
        return repository.getAllTransactionsFiltered(
            type,
            categoryIds,
            sourceIds,
            fromTimestamp,
            toTimestamp
        )
    }
}
