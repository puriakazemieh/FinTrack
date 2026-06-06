package com.kazemieh.common

/**
 * Extension for Left-to-Right Mark to fix layout issues with RTL languages and symbols.
 */
fun String.withLRM(): String = "\u200E$this"

/**
 * Converts English digits to Persian digits.
 */
fun String.toPersianDigits(): String = this.map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
fun Long.toPersianDigits(): String = toString().toPersianDigits()
fun Int.toPersianDigits(): String = toLong().toPersianDigits()

/**
 * Formats a number with comma separators every 3 digits.
 * Example: 1250000 -> 1,250,000
 */
fun String.separateWithCommas(): String = this.reversed().chunked(3).joinToString(",").reversed()
fun Long.separateWithCommas(): String = toString().separateWithCommas()
fun Int.separateWithCommas(): String = toString().separateWithCommas()

/**
 * Formats a value as a currency/price with Persian digits and comma separators.
 * Example: 1250000 -> ۱,۲۵۰,۰۰۰
 */
fun Long.toPersianPrice(): String = separateWithCommas().toPersianDigits()
fun Int.toPersianPrice(): String = toLong().toPersianPrice()
fun String.toPersianPrice(): String = separateWithCommas().toPersianDigits()

/**
 * Formats a value with a sign (+/-), Persian digits, and comma separators.
 * Useful for displaying transaction amounts or balance changes.
 */
fun Long.toSignedPersianPrice(): String =
    when {
        this < 0 -> "- ${(-this).toPersianPrice()}".withLRM().toPersianDigits()
        this > 0 -> "+ ${this.toPersianPrice()}".withLRM().toPersianDigits()
        else -> "0".toPersianDigits()
    }
fun Int.toSignedPersianPrice(): String = toLong().toSignedPersianPrice()

/**
 * Helper to safely unwrap two nullable objects.
 */
inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}
