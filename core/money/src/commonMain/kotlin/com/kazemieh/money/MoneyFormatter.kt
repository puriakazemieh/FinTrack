package com.kazemieh.money

import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.separateWithCommas

object MoneyFormatter {
    fun format(
        amount: Long,
        currency: Currency = Currency.TOMAN,
        includeSymbol: Boolean = true,
        showPositiveSign: Boolean = false
    ): String {
        val digits = amount.toString().removePrefix("-").separateWithCommas().toPersianDigits()
        val sign = when {
            amount < 0 -> "-"
            amount > 0 && showPositiveSign -> "+"
            else -> ""
        }
        val formattedAmount = sign + digits
        return if (includeSymbol) {
            "$formattedAmount ${currency.symbol}"
        } else {
            formattedAmount
        }
    }
}
