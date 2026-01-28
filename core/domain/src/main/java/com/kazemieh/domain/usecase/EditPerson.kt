package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository

class EditPerson(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(person: Person): Int {
        return repository.updatePerson(person)
    }
}
