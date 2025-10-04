package com.kazemieh.database.mapper

import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionType

fun TransactionEntity.toTransaction(): Transaction =
    Transaction(
        id = id,
        amount = amount,
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        description = description,
        date = date,
        type = TransactionType.fromInt(type)
    )

fun Transaction.toTransactionEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        amount = amount,
        categoryId = categoryId,
        financialSourceId = financialSourceId,
        description = description,
        date = date,
        type = type.count
    )
