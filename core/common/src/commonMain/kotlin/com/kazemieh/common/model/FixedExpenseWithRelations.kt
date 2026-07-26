package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class FixedExpenseWithRelations(
    val expense: FixedExpense,
    val category: Category? = null,
    val source: Source? = null,
    val tags: List<Tag> = emptyList(),
    val persons: List<Person> = emptyList()
)
