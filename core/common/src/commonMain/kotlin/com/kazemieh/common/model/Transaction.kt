package com.kazemieh.common.model

import com.kazemieh.common.formatNumber
import com.kazemieh.common.toFa
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Transaction(
    val id: Long,
    val amount: Int,
    val amountTransfer: Int = 0,
    val categoryId: Long,
    val sourceId: Long,
    val sourceEndId: Long? = null,
    val description: String? = null,
    val timeStamp: Long = Clock.System.now().toEpochMilliseconds(),
    val date: String = "",
    val type: TransactionType
) {
    val formatedAmount: String
        get() = when (type) {
            TransactionType.INCOME -> "\u200E+ ${amount.formatNumber().toFa()}"
            TransactionType.EXPENSE -> "\u200E- ${amount.formatNumber().toFa()}"
            else -> amount.formatNumber().toFa()
        }

    val amountTransferFormated: String?
        get() = if (type == TransactionType.TRANSFER) amountTransfer.formatNumber().toFa() else null
}

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