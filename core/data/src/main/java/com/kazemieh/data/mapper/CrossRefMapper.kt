package com.kazemieh.data.mapper

import com.kazemieh.database.entity.TransactionTagCrossRef

fun Pair<Long, Long>.toCrossRef(): TransactionTagCrossRef =
    TransactionTagCrossRef(transactionId = first, tagId = second)
