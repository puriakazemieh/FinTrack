package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveMostUsedPersonsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(limit: Long = 5): Flow<List<Person>> {
        return repository.observeMostUsedPersons(limit)
    }
}
