package com.kazemieh.transaction.ui.component

import com.kazemieh.common.formatted
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionType
import com.kazemieh.model.TransactionWithRelations
import kotlinx.serialization.Serializable


@Serializable
data class TransactionUi(
    val id: Long,
    val formatedAmount: String,
    val amount: Int,
    val categoryId: Long,
    val financialSourceId: Long,
    val description: String? = null,
    val timeStamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
)

fun Transaction.toUi(): TransactionUi {
    return TransactionUi(
        id = id,
        amount = amount,
        formatedAmount = amount.formatted(),
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        description = description,
        timeStamp = timeStamp,
        type = type
    )
}

fun TransactionUi.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        categoryId = categoryId,
        financialSourceId = financialSourceId,
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
    val tags: String
)

fun TransactionWithRelations.toUi() = TransactionWithRelationsUi(
    transaction = transaction.toUi(),
    categoryName = category.name,
    financialSourceName = financialSource.name,
    tags = tags.joinToString { it.name }
)

fun TransactionWithRelations.toPieChartItem(): PieChartItem {
    return PieChartItem(
        id = category.id,
        label = category.name,
        value = transaction.amount.toLong()
    )
}

