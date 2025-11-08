package com.kazemieh.common

import ir.huri.jcal.JalaliCalendar
import java.util.Calendar
import java.util.Date

data class DateRange(
    val start: Long,
    val end: Long,
    val filterType: DateFilterType,
    val label: String
)

enum class Direction { NEXT, PREVIOUS }


object DateFilterHelper {

    fun getRange(
        type: DateFilterType,
        customFrom: Long? = null,
        customTo: Long? = null
    ): DateRange? {
        val today = JalaliCalendar()

        return when (type) {

            DateFilterType.TODAY -> {
                today.toDateRange(type)
            }

            DateFilterType.YESTERDAY -> {
                today.yesterday.toDateRange(type)
            }

            DateFilterType.TOMORROW -> {
                today.tomorrow.toDateRange(type)
            }

            DateFilterType.THIS_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7
                val start = today.getDateByDiff(-diffToStart)
                val end = start.getDateByDiff(6)
                start.toDateRange(end, type)
            }

            DateFilterType.LAST_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7
                val start = today.getDateByDiff(-diffToStart - 7)
                val end = start.getDateByDiff(6)
                start.toDateRange(end, type)
            }

            DateFilterType.NEXT_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7
                val start = today.getDateByDiff(-diffToStart + 7)
                val end = start.getDateByDiff(6)
                start.toDateRange(end, type)
            }

            DateFilterType.THIS_MONTH -> {
                val start = JalaliCalendar(today.year, today.month, 1)
                val end = JalaliCalendar(today.year, today.month, today.getMonthLength())
                start.toDateRange(end, type)
            }

            DateFilterType.LAST_MONTH -> {
                var year = today.year
                var month = today.month - 1
                if (month == 0) {
                    month = 12
                    year -= 1
                }
                val start = JalaliCalendar(year, month, 1)
                val end = JalaliCalendar(year, month, start.getMonthLength())
                start.toDateRange(end, type)
            }

            DateFilterType.NEXT_MONTH -> {
                var year = today.year
                var month = today.month + 1
                if (month == 13) {
                    month = 1
                    year += 1
                }
                val start = JalaliCalendar(year, month, 1)
                val end = JalaliCalendar(year, month, start.getMonthLength())
                start.toDateRange(end, type)
            }

            DateFilterType.CUSTOM_RANGE -> {
                if (customFrom != null && customTo != null) {
                    val text = getDateRangeText(customFrom, customTo, type)
                    DateRange(customFrom, customTo, type, text)
                } else null
            }
        }
    }


    fun getDateRangeText(
        start: Long,
        end: Long,
        filterType: DateFilterType
    ): String {
        val startJalali = JalaliCalendar(Date(start))
        val endJalali = JalaliCalendar(Date(end))

        return when (filterType) {
            DateFilterType.TODAY -> "امروز"
            DateFilterType.YESTERDAY -> "دیروز"
            DateFilterType.TOMORROW -> "فردا"
            DateFilterType.THIS_WEEK -> "این هفته"
            DateFilterType.LAST_WEEK -> "هفته قبل"
            DateFilterType.NEXT_WEEK -> "هفته بعد"
            DateFilterType.THIS_MONTH -> "این ماه"
            DateFilterType.LAST_MONTH -> "ماه قبل"
            DateFilterType.NEXT_MONTH -> "ماه بعد"

            DateFilterType.CUSTOM_RANGE -> {
                if (startJalali.day == 1 && endJalali.day == startJalali.monthLength) {
                    val monthName = startJalali.monthString
                    val today = JalaliCalendar()
                    if (startJalali.year != today.year) {
                        "$monthName ${startJalali.year}"
                    } else monthName
                } else if (startJalali == endJalali) {
                    "${startJalali.day} ${startJalali.monthString} ${startJalali.year}"
                } else {
                    val startText = "${startJalali.day} ${startJalali.monthString}"
                    val endText = "${endJalali.day} ${endJalali.monthString}"
                    "$startText تا $endText"
                }
            }
        }
    }


    fun shiftDateRange(
        start: Long?,
        end: Long?,
        filterType: DateFilterType,
        direction: Direction
    ): DateRange {

        val diff = if (direction == Direction.NEXT) 1 else -1

        val startJalali = if (start != null) JalaliCalendar(Date(start)) else JalaliCalendar()
        val endJalali = if (end != null) JalaliCalendar(Date(end)) else JalaliCalendar()
        val today = JalaliCalendar()

        val newStart: JalaliCalendar
        val newEnd: JalaliCalendar
        var newFilter = filterType

        when (filterType) {

            // ===== روزها =====
            DateFilterType.TODAY,
            DateFilterType.YESTERDAY,
            DateFilterType.TOMORROW -> {
                newStart = startJalali.getDateByDiff(diff)
                newEnd = newStart

                newFilter = when (newStart) {
                    today -> DateFilterType.TODAY
                    today.yesterday -> DateFilterType.YESTERDAY
                    today.tomorrow -> DateFilterType.TOMORROW
                    else -> DateFilterType.CUSTOM_RANGE
                }
            }

            DateFilterType.THIS_WEEK,
            DateFilterType.LAST_WEEK,
            DateFilterType.NEXT_WEEK -> {
                newStart = startJalali.getDateByDiff(diff * 7)
                newEnd = endJalali.getDateByDiff(diff * 7)

                val thisWeekStart = today.getDateByDiff(-(today.dayOfWeek - Calendar.SATURDAY))
                val thisWeekEnd = thisWeekStart.getDateByDiff(6)

                newFilter = when {
                    newStart == thisWeekStart -> DateFilterType.THIS_WEEK
                    newStart.getDateByDiff(7) == thisWeekStart -> DateFilterType.LAST_WEEK

                    newStart.getDateByDiff(-7) == thisWeekStart -> DateFilterType.NEXT_WEEK

                    else -> DateFilterType.CUSTOM_RANGE
                }
            }

            DateFilterType.THIS_MONTH,
            DateFilterType.LAST_MONTH,
            DateFilterType.NEXT_MONTH -> {
                var newMonth = startJalali.month + diff
                var newYear = startJalali.year

                if (newMonth > 12) {
                    newMonth = 1
                    newYear++
                } else if (newMonth < 1) {
                    newMonth = 12
                    newYear--
                }

                newStart = JalaliCalendar(newYear, newMonth, 1)
                newEnd = JalaliCalendar(newYear, newMonth, newStart.monthLength)

                newFilter = when (newYear) {
                    today.year if newMonth == today.month ->
                        DateFilterType.THIS_MONTH

                    today.year if newMonth == today.month - 1 ->
                        DateFilterType.LAST_MONTH

                    today.year if newMonth == today.month + 1 ->
                        DateFilterType.NEXT_MONTH

                    today.year + 1 if newMonth == 1 && today.month == 12 ->
                        DateFilterType.NEXT_MONTH

                    today.year - 1 if newMonth == 12 && today.month == 1 ->
                        DateFilterType.LAST_MONTH

                    else -> DateFilterType.CUSTOM_RANGE
                }
            }

            // ===== سایر بازه‌ها =====
            else -> {

                var daysBetween = 0
                var temp = JalaliCalendar(Date(start ?: 0))
                val endJ = JalaliCalendar(Date(end ?: 0))

                if (temp.day == 1 && endJ.day == temp.monthLength) {
                    var newMonth = startJalali.month + diff
                    var newYear = startJalali.year
                    if (newMonth > 12) {
                        newMonth = 1
                        newYear++
                    } else if (newMonth < 1) {
                        newMonth = 12
                        newYear--
                    }
                    newStart = JalaliCalendar(newYear, newMonth, 1)
                    newEnd = JalaliCalendar(newYear, newMonth, newStart.monthLength)
                } else {
                    while (temp != endJ) {
                        temp = temp.getDateByDiff(1)
                        daysBetween++
                    }


                    newStart = startJalali.getDateByDiff(diff * (daysBetween + 1))
                    newEnd = endJalali.getDateByDiff(diff * (daysBetween + 1))
                }
            }

        }

        val newStartMillis = newStart.toGregorian().timeInMillis
        val newEndMillis = newEnd.toGregorian().timeInMillis
        val label = getDateRangeText(newStartMillis, newEndMillis, newFilter)

        return DateRange(
            start = newStartMillis,
            end = newEndMillis,
            filterType = newFilter,
            label = label
        )
    }


    private fun JalaliCalendar.toDateRange(
        type: DateFilterType,
    ): DateRange {
        val gc = this.toGregorian()
        gc.set(Calendar.HOUR_OF_DAY, 0)
        gc.set(Calendar.MINUTE, 0)
        gc.set(Calendar.SECOND, 0)
        gc.set(Calendar.MILLISECOND, 0)
        val start = gc.timeInMillis

        gc.set(Calendar.HOUR_OF_DAY, 23)
        gc.set(Calendar.MINUTE, 59)
        gc.set(Calendar.SECOND, 59)
        gc.set(Calendar.MILLISECOND, 999)
        val end = gc.timeInMillis

        return DateRange(start, end, type, type.title)
    }

    private fun JalaliCalendar.toDateRange(
        endDate: JalaliCalendar,
        type: DateFilterType,
    ): DateRange {
        val startGc = this.toGregorian().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endGc = endDate.toGregorian().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return DateRange(startGc.timeInMillis, endGc.timeInMillis, type, type.title)
    }

}


enum class DateFilterType(val title: String) {
    TODAY("امروز"),
    YESTERDAY("دیروز"),
    TOMORROW("فردا"),
    THIS_WEEK("این هفته"),
    LAST_WEEK("هفته قبل"),
    NEXT_WEEK("هفته بعد"),
    THIS_MONTH("این ماه"),
    LAST_MONTH("ماه قبل"),
    NEXT_MONTH("ماه بعد"),
    CUSTOM_RANGE("بازه انتخابی")
}