package com.kazemieh.financialsource.ui

import com.kazemieh.common.formatted
import com.kazemieh.common.model.FinancialSource

data class FinancialSourceUi(
    val id: Long,
    val name: String,
    val formattedBalance: String,
    val type: Int
)

fun FinancialSource.toUi(): FinancialSourceUi {

    return FinancialSourceUi(
        id = id ?: 0,
        name = name,
        formattedBalance = balance.formatted(),
        type = type
    )
}
