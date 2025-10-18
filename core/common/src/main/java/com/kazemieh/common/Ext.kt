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


fun Int.toPositive(): Int =
    when {
        this < 0 -> this * -1
        this > 0 -> this
        else -> 0
    }