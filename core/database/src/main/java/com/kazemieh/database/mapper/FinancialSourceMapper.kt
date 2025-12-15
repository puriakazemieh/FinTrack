package com.kazemieh.database.mapper

import com.kazemieh.common.formatted
import com.kazemieh.database.entity.FinancialSourceEntity
import com.kazemieh.common.model.Source

fun FinancialSourceEntity.toFinancialSource(): Source = Source(
    id = id,
    name = name,
    balance = balance,
    cardNumber = cardNumber,
    description = description,
    formattedBalance = balance.formatted(),
    type = type
)

fun Source.toFinancialSourceEntity(): FinancialSourceEntity =
    FinancialSourceEntity(
        id = id,
        name = name,
        balance = balance,
        cardNumber = cardNumber,
        description = description,
        type = type
    )
