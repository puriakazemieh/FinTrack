package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class TransactionWithRelations(
    val transaction: Transaction,
    val category: Category,
    val source: Source,
    val sourceEnd: Source?,
    val tags: List<Tag>,
    val persons: List<Person>,
)
