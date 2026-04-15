package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Source

class AddSourceUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(source: Source): Long {
        return repository.addSource(source = source)
    }
}
