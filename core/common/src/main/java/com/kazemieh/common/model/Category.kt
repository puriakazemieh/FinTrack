package com.kazemieh.common.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: TransactionType,
)


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CategorySum(
    val categoryId: Long,
    val name: String,
    val totalAmount: Long,
    val type: TransactionType
)
