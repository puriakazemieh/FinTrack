package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class Transaction(
    val id: Long,
    val amount: Int,
    val amountTransfer: Int = 0,
    val categoryId: Long,
    val sourceId: Long,
    val sourceEndId: Long? = null,
    val description: String? = null,
    val timeStamp: Long = System.currentTimeMillis(),
    val date: String = "",
    val type: TransactionType,
    val formatedAmount: String = 0.toString(),
    val amountTransferFormated: String? = null,
)

@Serializable
enum class TransactionType(val count: Int) {
    ALL(0),
    INCOME(1),
    EXPENSE(2),
    TRANSFER(3);

    companion object {
        fun fromInt(value: Int) = entries.first { it.count == value }
    }
}