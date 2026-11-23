package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class SetDefaultSourceUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: Long): Boolean = repository.setDefaultSource(id)
}
