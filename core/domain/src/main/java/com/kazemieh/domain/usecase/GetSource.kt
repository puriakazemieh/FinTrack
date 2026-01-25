package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetSource(
    private val repository: TransactionRepository
) {
    operator fun invoke(sourceId: Long): Flow<Source?> {
        return repository.getSource(sourceId)
    }
}
