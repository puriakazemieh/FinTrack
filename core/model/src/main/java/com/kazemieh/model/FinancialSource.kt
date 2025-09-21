package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class FinancialSource(
    val id: Long? = null,
    val name: String,
    val balance: Int = 0,
    val cardNumber: String? = null,
    val description: String? = null,
    val type: Int? = null,
)