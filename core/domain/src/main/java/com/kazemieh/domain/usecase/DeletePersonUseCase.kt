package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository

class DeletePersonUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(deletePerson: Person, movePerson: Person?) {
        return repository.deletePerson(deletePerson, movePerson)
    }
}
