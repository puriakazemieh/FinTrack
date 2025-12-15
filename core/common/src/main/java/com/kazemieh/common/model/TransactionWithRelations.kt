package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionWithRelations(
    val transaction: Transaction,
    val categoryName: String,
    val financialSourceName: String,
    val tags: String,
    val persons: String
)
