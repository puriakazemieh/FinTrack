package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class FinancialSource(
    val id: Long,
    val name: String
)