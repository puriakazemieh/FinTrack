package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Person
import kotlinx.coroutines.flow.Flow

class GetAllPerson(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Flow<List<Person>> {
        return repository.getAllPersons()
    }
}
