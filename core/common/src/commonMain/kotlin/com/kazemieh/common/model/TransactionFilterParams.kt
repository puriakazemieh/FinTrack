package com.kazemieh.common.model


data class TransactionFilterParams(
    val type: Int? = null,
    val sources: Set<Source> = emptySet(),
    val isAllSources: Boolean = true,
    val categories: Set<Category> = emptySet(),
    val isAllCategories: Boolean = true,
    val tags: Set<Tag> = emptySet(),
    val isAllTags: Boolean = true,
    val persons: Set<Person> = emptySet(),
    val isAllPersons: Boolean = true,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null
)