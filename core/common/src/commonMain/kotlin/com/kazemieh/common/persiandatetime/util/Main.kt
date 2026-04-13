package com.kazemieh.common.persiandatetime.util

import com.kazemieh.common.persiandatetime.PersianDateConverterImpl


internal object Main {
    val persianDateConverter by lazy {
        PersianDateConverterImpl
    }

}