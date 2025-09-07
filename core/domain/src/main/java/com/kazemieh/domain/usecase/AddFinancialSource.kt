package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class AddFinancialSource(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(financialSourceName: String, balance: Double = 0.0): Long {
        return repository.insertFinancialSource(
            financialSourceName = financialSourceName,
            balance = balance
        )
    }
}
