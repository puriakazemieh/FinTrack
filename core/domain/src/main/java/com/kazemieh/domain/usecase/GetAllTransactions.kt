package com.kazemieh.domain.usecase

import androidx.paging.PagingData
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

class GetAllTransactions(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<PagingData<TransactionWithRelations>> {
        return repository.getAllTransactions()
    }
}
