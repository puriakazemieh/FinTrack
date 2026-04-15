package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Source

class DeleteSourceUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(deleteSource: Source, moveSource: Source?) {
        return repository.deleteSource(deleteSource, moveSource)
    }
}
