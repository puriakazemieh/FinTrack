package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long,
    val amount: Int,
    val categoryId: Long,
    val financialSourceId: Long,
    val description: String? = null,
    val timeStamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
)

enum class TransactionType(val count: Int) {
    ALL(0),
    INCOME(1),
    EXPENSE(2);

    companion object {
        fun fromInt(value: Int) = TransactionType.entries.first { it.count == value }
    }
}