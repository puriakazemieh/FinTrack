package com.kazemieh.common


import android.util.Log

fun <T> T.ld(param: String? = "param"): T {
    return this.apply {
        Log.d("949494", "$param = $this")
    }
}