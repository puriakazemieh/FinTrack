package com.kazemieh.domain.util

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType

fun Transaction.balanceImpact(): Map<Long, Long> = when (type) {
    TransactionType.INCOME -> mapOf(sourceId to amount)

    TransactionType.EXPENSE -> mapOf(sourceId to -amount)

    TransactionType.TRANSFER -> buildMap {
        put(sourceId, -(amount + amountTransfer))
        sourceEndId?.let { put(it, amount) }
    }

    else -> emptyMap()
}
