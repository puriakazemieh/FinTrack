package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Tag

class EditTag(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(tag: Tag): Int {
        return repository.updateTag(tag)
    }
}
