package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class GetTransactionAmountRangeUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Pair<Long, Long> = repository.getTransactionAmountRange()
}
