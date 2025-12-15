package com.kazemieh.domain.usecase

import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetCategorySum(
    private val repository: TransactionRepository
) {
    operator fun invoke(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> {
        return repository.getCategorySums(transactionFilterParams)
    }
}
