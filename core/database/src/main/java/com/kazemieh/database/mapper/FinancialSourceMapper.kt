package com.kazemieh.database.mapper

import com.kazemieh.database.entity.FinancialSourceEntity
import com.kazemieh.common.model.FinancialSource

fun FinancialSourceEntity.toFinancialSource(): FinancialSource = FinancialSource(
    id = id,
    name = name,
    balance = balance,
    cardNumber = cardNumber,
    description = description,
    type = type
)

fun FinancialSource.toFinancialSourceEntity(): FinancialSourceEntity =
    FinancialSourceEntity(
        id = id,
        name = name,
        balance = balance,
        cardNumber = cardNumber,
        description = description,
        type = type
    )
