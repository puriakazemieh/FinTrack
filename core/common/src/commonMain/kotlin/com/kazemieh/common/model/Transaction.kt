package com.kazemieh.common.model

import com.kazemieh.common.toPersianPrice
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Transaction(
    val id: Long,
    val amount: Long,
    val currencyCode: String = "IRT",
    val amountTransfer: Long = 0L,
    val categoryId: Long,
    val sourceId: Long,
    val sourceEndId: Long? = null,
    val relatedDebtId: Long? = null,
    val description: String? = null,
    val photoPath: String? = null,
    val timeStamp: Long = Clock.System.now().toEpochMilliseconds(),
    val date: String = "",
    val type: TransactionType,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity {

    val amountTransferFormated: String?
        get() = if (type == TransactionType.TRANSFER) amountTransfer.toPersianPrice() else null
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
