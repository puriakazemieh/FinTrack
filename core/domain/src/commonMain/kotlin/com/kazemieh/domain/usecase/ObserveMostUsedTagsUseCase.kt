package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveMostUsedTagsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(limit: Long = 5): Flow<List<Tag>> {
        return repository.observeMostUsedTags(limit)
    }
}
