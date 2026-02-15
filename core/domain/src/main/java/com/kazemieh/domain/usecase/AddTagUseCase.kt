package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Tag

class AddTagUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(tag: Tag): Long {
        return repository.addTag(tag)
    }
}
