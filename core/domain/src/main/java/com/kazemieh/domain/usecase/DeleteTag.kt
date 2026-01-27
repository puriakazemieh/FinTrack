package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.TransactionRepository

class DeleteTag(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(deleteTag: Tag, moveTag: Tag?) {
        return repository.deleteTag(deleteTag, moveTag)
    }
}
