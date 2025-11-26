package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.FinancialSource

class GetDefaultFinancialSourceUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): FinancialSource {
        return repository.getDefaultSource()
    }
}