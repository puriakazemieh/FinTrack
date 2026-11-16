package com.kazemieh.money

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {
    @Test
    fun negativeAmountsKeepTheMinusOutsideDigitGrouping() {
        assertEquals("-۱۵۰", MoneyFormatter.format(-150L, includeSymbol = false))
        assertEquals("-۱۵۰,۰۰۰", MoneyFormatter.format(-150_000L, includeSymbol = false))
        assertEquals("-۹,۲۲۳,۳۷۲,۰۳۶,۸۵۴,۷۷۵,۸۰۸", MoneyFormatter.format(Long.MIN_VALUE, includeSymbol = false))
    }

    @Test
    fun signedAmountsAndZeroRemainDistinct() {
        assertEquals("+۱۵۰", MoneyFormatter.format(150L, includeSymbol = false, showPositiveSign = true))
        assertEquals("۰", MoneyFormatter.format(0L, includeSymbol = false, showPositiveSign = true))
        assertEquals("۱۵۰", MoneyFormatter.format(150L, includeSymbol = false))
    }
}
