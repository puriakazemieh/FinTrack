package com.kazemieh.domain.usecase

import com.kazemieh.common.model.FinancialSource
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetAllFinancialSource(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Flow<List<FinancialSource>> {
        return repository.getAllFinancialSource()
    }
}
