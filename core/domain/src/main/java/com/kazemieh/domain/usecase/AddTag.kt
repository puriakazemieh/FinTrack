package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository

class AddTag(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(tagName: String) {
        return repository.insertTag(tagName)
    }
}
