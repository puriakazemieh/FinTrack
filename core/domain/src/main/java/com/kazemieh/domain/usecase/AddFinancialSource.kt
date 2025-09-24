package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.FinancialSource

class AddFinancialSource(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(financialSource: FinancialSource): Long {
        return repository.insertFinancialSource(financialSource = financialSource)
    }
}
