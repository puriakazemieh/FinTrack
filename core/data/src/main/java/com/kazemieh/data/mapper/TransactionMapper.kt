package com.kazemieh.data.mapper

import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.model.Transaction

fun TransactionEntity.toTransaction(): Transaction =
    Transaction(id, amount, categoryId, financialSourceId, description, date)

fun Transaction.toTransactionEntity(): TransactionEntity =
    TransactionEntity(id, amount, categoryId, financialSourceId, description, date)
