package com.kazemieh.database.mapper

import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType

fun TransactionEntity.toTransaction(): Transaction =
    Transaction(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer ?: 0,
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        financialSourceEndId = financialSourceEndId,
        description = description,
        timeStamp = timeStamp,
        type = TransactionType.fromInt(type)
    )

fun Transaction.toTransactionEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        amount = amount,
        amountTransfer = amountTransfer,
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        financialSourceEndId = financialSourceEndId,
        description = description,
        timeStamp = timeStamp,
        type = type.count
    )
