package com.kazemieh.designsystem.component.jalali

import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.dayOfWeekIndex
import com.kazemieh.common.persiandatetime.extensions.isLeapYear
import com.kazemieh.common.persiandatetime.extensions.monthLength
import com.kazemieh.common.persiandatetime.extensions.persianDayOfWeek
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.persiandatetime.extensions.toLocalDate
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * مدل تاریخ شمسی (Jalali) – پوسته روی PersianDateTime برای استفاده در UI
 */
class JalaliCalendar(val year: Int, val month: Int, val day: Int) {

    constructor() : this(
        today().year,
        today().month,
        today().day
    )

    private val persianDateTime: PersianDateTime
        get() = PersianDateTime(year, month, day)

    val monthLength: Int
        get() = persianDateTime.monthLength()

    val monthString: String
        get() = persianDateTime.persianMonth().displayName

    val dayOfWeek: Int
        get() = persianDateTime.dayOfWeekIndex

    val dayOfWeekString: String
        get() = persianDateTime.persianDayOfWeek().displayName

    fun isLeapYear(): Boolean = persianDateTime.isLeapYear()

    fun toGregorian(): Triple<Int, Int, Int> {
        val ld = persianDateTime.toLocalDate()
        return Triple(ld.year, ld.monthNumber, ld.dayOfMonth)
    }

    fun getYesterday(): JalaliCalendar = getDateByDiff(-1)
    fun getTomorrow(): JalaliCalendar = getDateByDiff(1)

    private fun getDateByDiff(diff: Int): JalaliCalendar {
        val ld = persianDateTime.toLocalDate().plus(diff, DateTimeUnit.DAY)
        return fromGregorian(ld.year, ld.monthNumber, ld.dayOfMonth)
    }

    override fun toString(): String {
        val y = year.toString().padStart(4, '0')
        val m = month.toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JalaliCalendar) return false
        return year == other.year && month == other.month && day == other.day
    }

    override fun hashCode(): Int = (year * 372) + (month * 31) + day

    fun isBefore(other: JalaliCalendar): Boolean {
        return this.toTimestamp() < other.toTimestamp()
    }

    fun isAfter(other: JalaliCalendar): Boolean {
        return this.toTimestamp() > other.toTimestamp()
    }

    fun isEqual(other: JalaliCalendar): Boolean {
        return this.toTimestamp() == other.toTimestamp()
    }

    fun isBeforeOrEqual(other: JalaliCalendar): Boolean {
        return this.toTimestamp() <= other.toTimestamp()
    }

    fun isAfterOrEqual(other: JalaliCalendar): Boolean {
        return this.toTimestamp() >= other.toTimestamp()
    }

    fun toTimestamp(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long {
        val (gYear, gMonth, gDay) = toGregorian()
        return LocalDate(gYear, gMonth, gDay).atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    companion object {
        fun today(): JalaliCalendar {
            val now = kotlin.time.Clock.System.now()
            val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            return fromGregorian(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        }

        fun fromGregorian(year: Int, month: Int, day: Int): JalaliCalendar {
            val pdt = LocalDate(year, month, day).toPersianDateTime()
            return JalaliCalendar(pdt.year, pdt.month, pdt.day)
        }

        fun fromTimestamp(timestamp: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): JalaliCalendar {
            val localDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone).date
            return fromGregorian(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        }

        fun fromEpochMilliseconds(epochMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): JalaliCalendar {
            val localDate = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
            return fromGregorian(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        }
    }
}
