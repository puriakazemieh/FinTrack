package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class UpdateTagPositionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(positions: Map<Long, Int>) {
        repository.updateTagPositions(positions)
    }
}
