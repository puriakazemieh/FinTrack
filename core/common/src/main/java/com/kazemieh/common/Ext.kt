package com.kazemieh.common


import android.util.Log
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun <T> T.ld(param: String? = "param"): T {
    return this.apply {
        Log.d("949494", "$param = $this")
    }
}

//fun Int.formatNumber(): String {
//    val formatter = NumberFormat.getInstance(Locale.getDefault())
//    return formatter.format(this)
//}

fun Int.formatNumber(): String {
    val decimalFormat = DecimalFormat("#,###")
    return decimalFormat.format(this)
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
    this.toIntOrNull()?.let { number ->
        return String.format(Locale.getDefault(), "%,d", number)
    } ?: return ""
}


inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}