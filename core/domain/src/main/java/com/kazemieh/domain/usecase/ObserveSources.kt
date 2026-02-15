package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetAllSource(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Flow<List<Source>> {
        return repository.observeSources()
    }
}
