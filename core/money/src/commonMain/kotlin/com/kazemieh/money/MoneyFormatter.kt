package com.kazemieh.money

import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.separateWithCommas

object MoneyFormatter {
    fun format(amount: Long, currency: Currency = Currency.TOMAN, includeSymbol: Boolean = true): String {
        val formattedAmount = amount.separateWithCommas().toPersianDigits()
        return if (includeSymbol) {
            "$formattedAmount ${currency.symbol}"
        } else {
            formattedAmount
        }
    }
}
