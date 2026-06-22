package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository

class GetPersonByIdUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Person? {
        return repository.getPersonById(id)
    }
}
