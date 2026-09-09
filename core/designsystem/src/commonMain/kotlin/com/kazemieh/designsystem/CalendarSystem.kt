package com.kazemieh.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import com.kazemieh.common.toPersianDigits
import com.kazemieh.jalali.JalaliCalendar
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Calendar used to present and pick dates. Persisted data remains an epoch timestamp. */
enum class CalendarSystem {
    JALALI,
    GREGORIAN;

    companion object {
        fun fromName(value: String): CalendarSystem =
            entries.firstOrNull { it.name == value } ?: JALALI
    }
}

val LocalCalendarSystem = staticCompositionLocalOf { CalendarSystem.JALALI }

/** Formats a persisted epoch timestamp without changing the underlying stored date. */
fun formatCalendarDate(
    timestamp: Long,
    calendarSystem: CalendarSystem,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String = when (calendarSystem) {
    CalendarSystem.JALALI -> JalaliCalendar.fromTimestamp(timestamp, timeZone).let {
        "${it.day.toPersianDigits()} / ${it.monthString} / ${it.year.toPersianDigits()}"
    }
    CalendarSystem.GREGORIAN -> Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone).date.let {
        "${it.dayOfMonth.toString().padStart(2, '0')} / ${it.monthNumber.toString().padStart(2, '0')} / ${it.year}"
    }
}

/** Month length for the proleptic Gregorian calendar, independent of platform APIs. */
fun gregorianMonthLength(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> error("Invalid Gregorian month: $month")
}
