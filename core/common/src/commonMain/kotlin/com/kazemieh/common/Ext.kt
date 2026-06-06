package com.kazemieh.common



fun String.chunked3(): String {
    return this.reversed().chunked(3).joinToString(",").reversed()
}

fun String.withLRM(): String = "\u200E$this"

fun Int.formatted(): String = toLong().formatted()

fun Long.formatNumberLong(): String {
    return toString().reversed().chunked(3).joinToString(",").reversed().toFa()
}

fun Long.formatted(): String =
    when {
        this < 0 -> "- ${(-this).formatNumber()}".withLRM().toFa()
        this > 0 -> "+ ${this.formatNumber()}".withLRM().toFa()
        else -> "0".toFa()
    }


fun String.toPrice(): String {
    return this.chunked3()
}

fun Long.toFa(): String = toString().map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
fun Int.toFa(): String = this.toLong().toFa()
fun String.toFa(): String = this.map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")

fun Long.toFormattedFa(): String = formatNumber().toFa()
fun Long.formatNumber(): String = toString().chunked3()






inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}