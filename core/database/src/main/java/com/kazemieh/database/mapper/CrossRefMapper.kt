package com.kazemieh.database.mapper

import com.kazemieh.database.entity.TransactionTagCrossRef

fun Pair<Long, Long>.toCrossRef(): TransactionTagCrossRef =
    TransactionTagCrossRef(transactionId = first, tagId = second)
