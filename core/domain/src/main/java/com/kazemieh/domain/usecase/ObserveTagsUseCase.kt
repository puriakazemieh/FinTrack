package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Tag
import kotlinx.coroutines.flow.Flow

class ObserveTagsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Flow<List<Tag>> {
        return repository.observeTags()
    }
}
