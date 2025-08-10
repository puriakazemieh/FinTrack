package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag
import kotlinx.coroutines.flow.Flow

class GetAllFinancialSource(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Flow<List<FinancialSource>> {
        return repository.getAllFinancialSource()
    }
}
