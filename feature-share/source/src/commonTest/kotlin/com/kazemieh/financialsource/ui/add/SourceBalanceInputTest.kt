package com.kazemieh.financialsource.ui.add

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.*

class SourceBalanceInputTest {
    @Test
    fun negativeCanBeEnteredOneCharacterAtATime() {
        val text = "-150000"
        assertEquals(listOf(0L, -1L, -15L, -150L, -1500L, -15000L, -150000L),
            (1..text.length).map { parseSourceBalance(text.take(it)) })
    }

    @Test
    fun acceptsZeroSignsPersianAndArabicDigits() {
        assertEquals(0L, parseSourceBalance(""))
        assertEquals(0L, parseSourceBalance("+"))
        assertEquals(0L, parseSourceBalance("0"))
        assertEquals(-150000L, parseSourceBalance("−۱۵۰۰۰۰"))
        assertEquals(450000L, parseSourceBalance("+٤٥٠٠٠٠"))
    }

    @Test
    fun overflowOrInvalidInputDoesNotBecomeZero() {
        assertEquals(Long.MIN_VALUE, parseSourceBalance(Long.MIN_VALUE.toString()))
        assertEquals(Long.MAX_VALUE, parseSourceBalance(Long.MAX_VALUE.toString()))
        assertNull(parseSourceBalance("9223372036854775808"))
        assertNull(parseSourceBalance("-9223372036854775809"))
        assertNull(parseSourceBalance("1-2"))
        assertNull(parseSourceBalance("12a"))
    }

    @Test
    fun signIsNotGroupedAndEveryCursorPositionRoundTrips() {
        val cases = mapOf("" to "", "-" to "-", "+" to "+", "0" to "۰",
            "-150000" to "-۱۵۰,۰۰۰", "+450000" to "+۴۵۰,۰۰۰", "−۱۲۳۴" to "-۱,۲۳۴",
            Long.MIN_VALUE.toString() to "-۹,۲۲۳,۳۷۲,۰۳۶,۸۵۴,۷۷۵,۸۰۸")
        for ((raw, expected) in cases) {
            val rendered = SourceBalanceTransformation().filter(AnnotatedString(raw))
            assertEquals(expected, rendered.text.text)
            for (offset in 0..raw.length) {
                val position = rendered.offsetMapping.originalToTransformed(offset)
                assertTrue(position in 0..expected.length)
                assertEquals(offset, rendered.offsetMapping.transformedToOriginal(position), raw)
            }
            for (offset in 0..expected.length) {
                assertTrue(rendered.offsetMapping.transformedToOriginal(offset) in 0..raw.length)
            }
        }
    }
}
