package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Source

class AddFinancialSource(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(source: Source): Long {
        return repository.insertFinancialSource(source = source)
    }
}
