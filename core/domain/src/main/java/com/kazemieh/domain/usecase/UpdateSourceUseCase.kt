package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Source

class UpdateSourceUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(source: Source): Int {
        return repository.updateSource(source)
    }
}
