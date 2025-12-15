package com.kazemieh.database.mapper

import com.kazemieh.common.formatted
import com.kazemieh.common.model.Source
import com.kazemieh.database.entity.SourceEntity

fun SourceEntity.toSource(): Source = Source(
    id = id,
    name = name,
    balance = balance,
    cardNumber = cardNumber,
    description = description,
    formattedBalance = balance.formatted(),
    type = type
)

fun Source.toSourceEntity(): SourceEntity =
    SourceEntity(
        id = id ?: 0,
        name = name,
        balance = balance,
        cardNumber = cardNumber,
        description = description,
        type = type
    )
