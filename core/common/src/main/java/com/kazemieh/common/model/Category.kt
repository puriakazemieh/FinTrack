package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: TransactionType,
)