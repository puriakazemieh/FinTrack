package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long,
    val amount: Double,
    val category: String,
    val financialSource: String,
    val description: String?,
    val tags: List<String>,
    val date: String
)
