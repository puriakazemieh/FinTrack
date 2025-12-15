package com.kazemieh.common.model


data class TransactionFilterParams(
    val type: Int? = null,
    val sources: Set<Source> = emptySet(),
    val categories: Set<Category> = emptySet(),
    val tags: Set<Tag> = emptySet(),
    val persons: Set<Person> = emptySet(),
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null
)