package com.kazemieh.transaction.ui.component

import com.kazemieh.common.formatted
import com.kazemieh.common.formattedOrNull
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import kotlinx.serialization.Serializable


@Serializable
data class TransactionUi(
    val id: Long,
    val formatedAmount: String,
    val amount: Int,
    val amountTransfer: Int = 0,
    val amountTransferFormated: String? = null,
    val categoryId: Long,
    val financialSourceId: Long,
    val financialSourceEndId: Long? = null,
    val description: String? = null,
    val timeStamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
)

fun Transaction.toUi(): TransactionUi {
    return TransactionUi(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer,
        formatedAmount = amount.formatted(),
        amountTransferFormated = amountTransfer.formattedOrNull(),
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        financialSourceEndId = financialSourceEndId,
        description = description,
        timeStamp = timeStamp,
        type = type
    )
}

fun TransactionUi.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer,
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        financialSourceEndId = financialSourceEndId,
        description = description,
        timeStamp = timeStamp,
        type = type
    )
}


@Serializable
data class TransactionWithRelationsUi(
    val transaction: TransactionUi,
    val categoryName: String,
    val financialSourceName: String,
    val tags: String,
    val persons: String
)

fun TransactionWithRelations.toUi() = TransactionWithRelationsUi(
    transaction = transaction.toUi(),
    categoryName = category.name,
    financialSourceName = financialSource.name,
    tags = tags.joinToString { it.name },
    persons = persons.joinToString { it.name }
)

fun TransactionWithRelations.toPieChartItem(): PieChartItem {
    return PieChartItem(
        id = category.id,
        label = category.name,
        value = transaction.amount.toLong()
    )
}

