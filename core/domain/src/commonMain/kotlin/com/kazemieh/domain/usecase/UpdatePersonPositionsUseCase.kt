package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class UpdatePersonPositionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(positions: Map<Long, Int>) {
        repository.updatePersonPositions(positions)
    }
}
