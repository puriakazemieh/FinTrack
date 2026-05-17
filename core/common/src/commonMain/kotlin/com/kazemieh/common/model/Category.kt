package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: TransactionType = TransactionType.INCOME,
    val colorId: Int,
    val iconId: Int
)


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class CategorySum(
    val categoryId: Long,
    val name: String,
    val totalAmount: Long,
    val type: TransactionType,
    val colorId: Int,
    val iconId: Int
)
