package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Source

class GetDefaultFinancialSourceUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Source? {
        return repository.getDefaultSource()
    }
}