package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: TransactionType,
)


@Serializable
data class CategorySum(
    val categoryId: Long,
    val name: String,
    val totalAmount: Long,
    val type: TransactionType
)
