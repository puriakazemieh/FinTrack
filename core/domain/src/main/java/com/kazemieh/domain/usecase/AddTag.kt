package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Tag

class AddTag(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(tag: Tag): Long {
        return repository.insertTag(tag)
    }
}
