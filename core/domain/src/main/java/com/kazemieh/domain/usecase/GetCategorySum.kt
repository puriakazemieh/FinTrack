package com.kazemieh.domain.usecase

import com.kazemieh.common.model.CategorySum
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetCategorySum(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        tagIds: List<Int> = emptyList(),
        personIds: List<Int> = emptyList(),
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<List<CategorySum>> {
        return repository.getCategorySums(
            type = type,
            categoryIds = categoryIds,
            sourceIds = sourceIds,
            tagIds = tagIds,
            personIds = personIds,
            fromTimestamp = fromTimestamp,
            toTimestamp = toTimestamp
        )
    }
}
