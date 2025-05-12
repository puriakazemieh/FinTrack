package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long,
    val amount: Double,
    val categoryId: Long,
    val financialSourceId: Long,
    val description: String? = null,
    val date: String
)
