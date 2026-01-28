package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository

class DeletePerson(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(deletePerson: Person, movePerson: Person?) {
        return repository.deletePerson(deletePerson, movePerson)
    }
}
