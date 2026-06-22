package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository

class GetSourceByIdentifierUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(identifier: String): Source? {
        return repository.getSourceByIdentifier(identifier)
    }
}
