package com.kazemieh.common

import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.minus
import com.kazemieh.common.persiandatetime.extensions.monthLength
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.toLocalDate
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

enum class Direction { NEXT, PREVIOUS }

@Serializable
sealed interface DateRangeLabel {
    @Serializable
    data class Text(val value: String) : DateRangeLabel

    @Serializable
    data class Filter(val type: DateFilterType) : DateRangeLabel
}

@Serializable
data class DateRange(
    val start: Long,
    val end: Long,
    val filterType: DateFilterType,
    val label: DateRangeLabel
)

@Serializable
enum class DateFilterType {
    TODAY, YESTERDAY, TOMORROW,
    THIS_WEEK, LAST_WEEK, NEXT_WEEK,
    THIS_MONTH, LAST_MONTH, NEXT_MONTH,
    THIS_YEAR, LAST_YEAR, NEXT_YEAR,
    CUSTOM_RANGE
}

object DateFilterHelper {


    fun getRange(
        type: DateFilterType,
        customFrom: Long? = null,
        customTo: Long? = null,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): DateRange? {

        val today = nowPersianDate(timeZone)

        return when (type) {
            DateFilterType.TODAY -> today.toDateRange(type, timeZone)

            DateFilterType.YESTERDAY ->
                today.minus(1, DateTimeUnit.DAY).toDateRange(type, timeZone)

            DateFilterType.TOMORROW ->
                today.plus(1, DateTimeUnit.DAY).toDateRange(type, timeZone)

            DateFilterType.THIS_WEEK -> {
                val start = startOfWeek(today, timeZone)
                val end = start.plus(6, DateTimeUnit.DAY)
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.LAST_WEEK -> {
                val thisWeekStart = startOfWeek(today, timeZone)
                val start = thisWeekStart.minus(7, DateTimeUnit.DAY)
                val end = start.plus(6, DateTimeUnit.DAY)
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.NEXT_WEEK -> {
                val thisWeekStart = startOfWeek(today, timeZone)
                val start = thisWeekStart.plus(7, DateTimeUnit.DAY)
                val end = start.plus(6, DateTimeUnit.DAY)
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.THIS_MONTH -> {
                val start = PersianDateTime(today.year, today.month, 1)
                val end = PersianDateTime(today.year, today.month, start.monthLength())
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.LAST_MONTH -> {
                val start =
                    PersianDateTime(today.year, today.month, 1).minus(DatePeriod(months = 1))
                val start2 = PersianDateTime(start.year, start.month, 1)
                val end = PersianDateTime(start2.year, start2.month, start2.monthLength())
                start2.toDateRange(end, type, timeZone)
            }

            DateFilterType.NEXT_MONTH -> {
                val start = PersianDateTime(today.year, today.month, 1).plus(DatePeriod(months = 1))
                val start2 = PersianDateTime(start.year, start.month, 1)
                val end = PersianDateTime(start2.year, start2.month, start2.monthLength())
                start2.toDateRange(end, type, timeZone)
            }

            DateFilterType.THIS_YEAR -> {
                val start = PersianDateTime(today.year, 1, 1)
                val end = PersianDateTime(today.year, 12, PersianDateTime(today.year, 12, 1).monthLength())
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.LAST_YEAR -> {
                val start = PersianDateTime(today.year - 1, 1, 1)
                val end = PersianDateTime(today.year - 1, 12, PersianDateTime(today.year - 1, 12, 1).monthLength())
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.NEXT_YEAR -> {
                val start = PersianDateTime(today.year + 1, 1, 1)
                val end = PersianDateTime(today.year + 1, 12, PersianDateTime(today.year + 1, 12, 1).monthLength())
                start.toDateRange(end, type, timeZone)
            }

            DateFilterType.CUSTOM_RANGE -> {
                if (customFrom != null && customTo != null) {
                    val label = getDateRangeLabel(customFrom, customTo, type, timeZone)
                    DateRange(customFrom, customTo, type, label)
                } else null
            }
        }
    }

    fun shiftDateRange(
        start: Long?,
        end: Long?,
        filterType: DateFilterType,
        direction: Direction,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): DateRange {

        val step = if (direction == Direction.NEXT) 1 else -1

        val startP = start?.let { persianDateFromMillis(it, timeZone) } ?: nowPersianDate(timeZone)
        val endP = end?.let { persianDateFromMillis(it, timeZone) } ?: nowPersianDate(timeZone)
        val today = nowPersianDate(timeZone)

        var newStart: PersianDateTime
        var newEnd: PersianDateTime
        var newFilter: DateFilterType

        when (filterType) {

            DateFilterType.TODAY,
            DateFilterType.YESTERDAY,
            DateFilterType.TOMORROW -> {
                newStart = startP.plus(step, DateTimeUnit.DAY)
                newEnd = newStart
            }

            DateFilterType.THIS_WEEK,
            DateFilterType.LAST_WEEK,
            DateFilterType.NEXT_WEEK -> {
                newStart = startP.plus(step * 7, DateTimeUnit.DAY)
                newEnd = endP.plus(step * 7, DateTimeUnit.DAY)
            }

            DateFilterType.THIS_MONTH,
            DateFilterType.LAST_MONTH,
            DateFilterType.NEXT_MONTH -> {
                val monthBase = PersianDateTime(startP.year, startP.month, 1).plus(DatePeriod(months = step))
                newStart = PersianDateTime(monthBase.year, monthBase.month, 1)
                newEnd = PersianDateTime(monthBase.year, monthBase.month, newStart.monthLength())
            }

            DateFilterType.THIS_YEAR,
            DateFilterType.LAST_YEAR,
            DateFilterType.NEXT_YEAR -> {
                val newYear = startP.year + step
                newStart = PersianDateTime(newYear, 1, 1)
                newEnd = PersianDateTime(newYear, 12, PersianDateTime(newYear, 12, 1).monthLength())
            }

            else -> {
                val isFullMonth = startP.day == 1 && endP.day == startP.monthLength()
                if (isFullMonth) {
                    val monthBase = PersianDateTime(startP.year, startP.month, 1).plus(DatePeriod(months = step))
                    newStart = PersianDateTime(monthBase.year, monthBase.month, 1)
                    newEnd = PersianDateTime(monthBase.year, monthBase.month, newStart.monthLength())
                } else {
                    val startG = startP.toLocalDate()
                    val endG = endP.toLocalDate()
                    val daysBetween = endG.toEpochDays() - startG.toEpochDays()
                    val shift = (daysBetween + 1).toInt() * step

                    newStart = startP.plus(shift, DateTimeUnit.DAY)
                    newEnd = endP.plus(shift, DateTimeUnit.DAY)
                }
            }
        }

        // Unified identification of the new filter type relative to today
        val thisMonthStart = PersianDateTime(today.year, today.month, 1)
        val lastMonthStart = thisMonthStart.minus(DatePeriod(months = 1))
        val nextMonthStart = thisMonthStart.plus(DatePeriod(months = 1))

        val thisYearStart = PersianDateTime(today.year, 1, 1)
        val lastYearStart = PersianDateTime(today.year - 1, 1, 1)
        val nextYearStart = PersianDateTime(today.year + 1, 1, 1)

        val thisWeekStart = startOfWeek(today, timeZone)
        val lastWeekStart = thisWeekStart.minus(7, DateTimeUnit.DAY)
        val nextWeekStart = thisWeekStart.plus(7, DateTimeUnit.DAY)

        newFilter = when {
            newStart.isSameDay(today) && newEnd.isSameDay(today) -> DateFilterType.TODAY
            newStart.isSameDay(today.minus(1, DateTimeUnit.DAY)) && newEnd.isSameDay(today.minus(1, DateTimeUnit.DAY)) -> DateFilterType.YESTERDAY
            newStart.isSameDay(today.plus(1, DateTimeUnit.DAY)) && newEnd.isSameDay(today.plus(1, DateTimeUnit.DAY)) -> DateFilterType.TOMORROW

            newStart.isSameDay(thisWeekStart) && newEnd.isSameDay(thisWeekStart.plus(6, DateTimeUnit.DAY)) -> DateFilterType.THIS_WEEK
            newStart.isSameDay(lastWeekStart) && newEnd.isSameDay(lastWeekStart.plus(6, DateTimeUnit.DAY)) -> DateFilterType.LAST_WEEK
            newStart.isSameDay(nextWeekStart) && newEnd.isSameDay(nextWeekStart.plus(6, DateTimeUnit.DAY)) -> DateFilterType.NEXT_WEEK

            newStart.isSameMonth(thisMonthStart) && newEnd.day == newStart.monthLength() -> DateFilterType.THIS_MONTH
            newStart.isSameMonth(lastMonthStart) && newEnd.day == newStart.monthLength() -> DateFilterType.LAST_MONTH
            newStart.isSameMonth(nextMonthStart) && newEnd.day == newStart.monthLength() -> DateFilterType.NEXT_MONTH

            newStart.isSameYear(thisYearStart) && newStart.day == 1 && newStart.month == 1 && newEnd.month == 12 && newEnd.day == newEnd.monthLength() -> DateFilterType.THIS_YEAR
            newStart.isSameYear(lastYearStart) && newStart.day == 1 && newStart.month == 1 && newEnd.month == 12 && newEnd.day == newEnd.monthLength() -> DateFilterType.LAST_YEAR
            newStart.isSameYear(nextYearStart) && newStart.day == 1 && newStart.month == 1 && newEnd.month == 12 && newEnd.day == newEnd.monthLength() -> DateFilterType.NEXT_YEAR

            else -> DateFilterType.CUSTOM_RANGE
        }

        val newStartMillis = startOfDayMillis(newStart, timeZone)
        val newEndMillis = endOfDayMillis(newEnd, timeZone)
        val label = getDateRangeLabel(newStartMillis, newEndMillis, newFilter, timeZone)

        return DateRange(
            start = newStartMillis,
            end = newEndMillis,
            filterType = newFilter,
            label = label
        )
    }

    private fun PersianDateTime.isSameDay(other: PersianDateTime): Boolean =
        this.year == other.year && this.month == other.month && this.day == other.day

    private fun PersianDateTime.isSameMonth(other: PersianDateTime): Boolean =
        this.year == other.year && this.month == other.month

    private fun PersianDateTime.isSameYear(other: PersianDateTime): Boolean =
        this.year == other.year


    fun getDateText(
        dateMillis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val d = persianDateFromMillis(dateMillis, timeZone)
        val monthName = d.persianMonth().displayName
        return "${d.day} $monthName ${d.year}"
    }

    fun getDateRangeLabel(
        startMillis: Long,
        endMillis: Long,
        filterType: DateFilterType,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): DateRangeLabel {

        val startP = persianDateFromMillis(startMillis, timeZone)
        val endP = persianDateFromMillis(endMillis, timeZone)

        val daysBetween = (Instant.fromEpochMilliseconds(endMillis).toLocalDateTime(timeZone).date.toEpochDays() -
                Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(timeZone).date.toEpochDays()) + 1

        if (filterType != DateFilterType.CUSTOM_RANGE) {
            return DateRangeLabel.Filter(filterType)
        }

        val startMonthName = startP.persianMonth().displayName
        val endMonthName = endP.persianMonth().displayName

        val isFullMonth = startP.day == 1 && endP.day == startP.monthLength()

        return when {
            isFullMonth -> {
                val today = nowPersianDate(timeZone)
                if (startP.year != today.year) {
                    DateRangeLabel.Text("$startMonthName ${startP.year}")
                } else {
                    DateRangeLabel.Text(startMonthName)
                }
            }

            startP == endP -> {
                DateRangeLabel.Text("${startP.day} $startMonthName ${startP.year}")
            }

            else -> {
                DateRangeLabel.Text("${startP.day} $startMonthName تا ${endP.day} $endMonthName")
            }
        }
    }

    // -----------------------
    // Helpers (KMP-safe)
    // -----------------------

    private fun nowPersianDate(timeZone: TimeZone): PersianDateTime {
        val nowLocalDate = Clock.System.now()
            .toLocalDateTime(timeZone)
            .date

        return nowLocalDate.toPersianDateTime()
    }

    fun persianDateFromMillis(millis: Long, timeZone: TimeZone): PersianDateTime {
        val localDate: LocalDate = Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(timeZone)
            .date
        return localDate.toPersianDateTime()
    }

    private fun startOfWeek(persian: PersianDateTime, timeZone: TimeZone): PersianDateTime {
        val g = persian.toLocalDate()
        val dow = g.dayOfWeek
        val diffToSaturday = ((dow.ordinal - DayOfWeek.SATURDAY.ordinal) + 7) % 7
        return persian.minus(diffToSaturday, DateTimeUnit.DAY)
    }

    private fun startOfDayMillis(persian: PersianDateTime, timeZone: TimeZone): Long {
        val g = persian.toLocalDate()
        return g.atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    private fun endOfDayMillis(persian: PersianDateTime, timeZone: TimeZone): Long {
        val g = persian.toLocalDate()
        val nextDayStart =
            g.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
        return nextDayStart - 1
    }

    private fun PersianDateTime.toDateRange(type: DateFilterType, timeZone: TimeZone): DateRange {
        val start = startOfDayMillis(this, timeZone)
        val end = endOfDayMillis(this, timeZone)
        return DateRange(start, end, type, DateRangeLabel.Filter(type))
    }

    private fun PersianDateTime.toDateRange(
        endDate: PersianDateTime,
        type: DateFilterType,
        timeZone: TimeZone
    ): DateRange {
        val start = startOfDayMillis(this, timeZone)
        val end = endOfDayMillis(endDate, timeZone)
        return DateRange(start, end, type, DateRangeLabel.Filter(type))
    }
}
