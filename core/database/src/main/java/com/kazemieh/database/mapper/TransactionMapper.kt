package com.kazemieh.database.mapper

import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.formatted
import com.kazemieh.common.formattedOrNull
import com.kazemieh.common.ld
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.entity.TransactionEntity

fun TransactionEntity.toTransaction(): Transaction {

    val formattedAmount = amount
        .let { if (type == TransactionType.EXPENSE.count) it * -1 else it }
        .formatted()


    return Transaction(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer ?: 0,
        categoryId = categoryId,
        sourceId = sourceId,
        sourceEndId = sourceEndId,
        description = description,
        timeStamp = timeStamp,
        date = DateFilterHelper.getDateText(timeStamp),
        formatedAmount = formattedAmount,
        amountTransferFormated = amountTransfer.formattedOrNull(),
        type = TransactionType.fromInt(type)
    )
}

fun Transaction.toTransactionEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer,
        categoryId = categoryId,
        sourceId = sourceId,
        sourceEndId = sourceEndId,
        description = description,
        timeStamp = timeStamp,
        type = type.count
    )
