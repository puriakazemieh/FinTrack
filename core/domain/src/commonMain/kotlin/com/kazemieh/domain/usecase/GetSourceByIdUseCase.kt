package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository

class GetSourceByIdUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Source? {
        return repository.getSourceById(id)
    }
}
