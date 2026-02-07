package com.kazemieh.common.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: TransactionType = TransactionType.INCOME,
    val colorId: Int? = null,
    val iconId: Int? = null
)


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CategorySum(
    val categoryId: Long,
    val name: String,
    val totalAmount: Long,
    val type: TransactionType
)
