package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveMostUsedSourcesUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(limit: Long = 5): Flow<List<Source>> {
        return repository.observeMostUsedSources(limit)
    }
}
