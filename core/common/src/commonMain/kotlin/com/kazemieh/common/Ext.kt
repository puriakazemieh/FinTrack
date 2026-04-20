package com.kazemieh.common

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.pow


fun Int.formatNumber(): String {
    return this.toString().chunked(3).reversed().joinToString(" ").reversed()
}


fun Int.formatted(): String =
    when {
        this < 0 -> "${(-this).formatNumber()} -"
        this > 0 -> "${this.formatNumber()} +"
        else -> "0"
    }

fun Int?.formattedOrNull(): String? {
    if (this == null) return null
    return when {
        this < 0 -> "${(-this).formatNumber()} -"
        this > 0 -> "${this.formatNumber()} +"
        else -> "0"
    }
}


fun Int.toPositive(): Int =
    when {
        this < 0 -> this * -1
        this > 0 -> this
        else -> 0
    }

fun String.toPrice(): String {
    return this.toIntOrNull()?.formatNumber() ?: ""
}


fun Double.toDegrees(): Double = this * 180.0 / PI
fun Float.toDegrees(): Float = this * 180f / PI.toFloat()

// تبدیل درجه به رادیان
fun Double.toRadians(): Double = this * PI / 180.0
fun Float.toRadians(): Float = this * PI.toFloat() / 180f

// نرمال‌سازی زاویه به بازه 0 تا 360
fun Float.normalizeAngle(): Float {
    var angle = this % 360f
    if (angle < 0) angle += 360f
    return angle
}

fun Double.normalizeAngle(): Double {
    var angle = this % 360.0
    if (angle < 0) angle += 360.0
    return angle
}


// فرمت عدد بدون اعشار
fun Float.formatToInt(): String = this.toInt().toString()

// فرمت عدد با اعشار مشخص
fun Float.formatDecimal(digits: Int = 0): String {
    val multiplier = 10.0.pow(digits)
    val rounded = floor((this * multiplier)) / multiplier
    return if (digits == 0) {
        rounded.toInt().toString()
    } else {
        val parts = rounded.toString().split(".")
        val decimalPart = parts.getOrElse(1) { "0" }.padEnd(digits, '0').take(digits)
        "${parts[0]}.$decimalPart"
    }
}

// commonMain
fun Double.format(digits: Int = 0): String {
    val multiplier = 10.0.pow(digits)
    val rounded = kotlin.math.round(this * multiplier) / multiplier
    return if (digits == 0) {
        rounded.toLong().toString()
    } else {
        val parts = rounded.toString().split('.')
        val integerPart = parts[0]
        val fractionalPart = parts.getOrElse(1) { "0" }.padEnd(digits, '0').take(digits)
        "$integerPart.$fractionalPart"
    }
}

fun Float.format(digits: Int = 0): String = this.toDouble().format(digits)


inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}