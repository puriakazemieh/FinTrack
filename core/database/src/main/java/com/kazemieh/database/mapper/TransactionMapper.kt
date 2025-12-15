package com.kazemieh.database.mapper

import com.kazemieh.common.formatted
import com.kazemieh.common.formattedOrNull
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType

fun TransactionEntity.toTransaction(): Transaction =
    Transaction(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer ?: 0,
        categoryId = categoryId,
        sourceId = sourceId,
        sourceEndId = sourceEndId,
        description = description,
        timeStamp = timeStamp,
        formatedAmount = amount.formatted(),
        amountTransferFormated = amountTransfer.formattedOrNull(),
        type = TransactionType.fromInt(type)
    )

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
