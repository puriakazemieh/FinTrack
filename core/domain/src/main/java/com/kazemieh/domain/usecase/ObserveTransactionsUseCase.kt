package com.kazemieh.domain.usecase

import androidx.paging.PagingData
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(transactionFilterParams: TransactionFilterParams): Flow<PagingData<TransactionWithRelations>> {
        return repository.observeTransactions(transactionFilterParams)
    }
}
