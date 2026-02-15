package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Person

class AddPersonUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(person: Person): Long {
        return repository.addPerson(person)
    }
}
