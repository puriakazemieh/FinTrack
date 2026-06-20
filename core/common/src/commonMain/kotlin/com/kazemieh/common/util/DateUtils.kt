package com.kazemieh.common.util

import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.toPersianDigits

object DateUtils {
    fun formatTimestamp(timestamp: Long): String {
        val pdt = PersianDateTime.parse(timestamp)
        return "${pdt.year}/${pdt.month.toString().padStart(2, '0')}/${pdt.day.toString().padStart(2, '0')} ${pdt.hour.toString().padStart(2, '0')}:${pdt.minute.toString().padStart(2, '0')}".toPersianDigits()
    }
    
    fun getRelativeTime(timestamp: Long, now: Long): String {
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "الان"
            diff < 3600_000 -> "${diff / 60_000} دقیقه پیش".toPersianDigits()
            diff < 86400_000 -> "${diff / 3600_000} ساعت پیش".toPersianDigits()
            else -> formatTimestamp(timestamp)
        }
    }
}
