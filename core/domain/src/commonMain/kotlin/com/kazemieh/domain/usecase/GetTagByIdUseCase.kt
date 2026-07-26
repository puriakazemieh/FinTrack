package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.TransactionRepository

class GetTagByIdUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Tag? {
        return repository.getTagById(id)
    }
}
