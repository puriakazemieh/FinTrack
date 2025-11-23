package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Person

class AddPerson(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(person: Person): Long {
        return repository.insertPerson(person)
    }
}
