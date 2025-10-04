package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long,
    val amount: Int,
    val categoryId: Long,
    val financialSourceId: Long,
    val description: String? = null,
    val date: String,
    val type: TransactionType,
)

enum class TransactionType(val count: Int) {
    INCOMING(1),
    OUTCOMING(2);

    companion object {
        fun fromInt(value: Int) = TransactionType.entries.first { it.count == value }
    }
}