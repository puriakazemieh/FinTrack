package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Page
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        transactionFilterParams: TransactionFilterParams,
        request: PageRequest
    ): Flow<Page<TransactionWithRelations>> {
        return repository.observeTransactions(transactionFilterParams, request)
    }
}
