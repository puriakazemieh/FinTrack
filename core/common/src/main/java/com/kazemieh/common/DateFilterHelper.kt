package com.kazemieh.common

import ir.huri.jcal.JalaliCalendar
import java.util.Calendar
import java.util.Date

data class DateRange(
    val start: Long,
    val end: Long,
    val filterType: DateFilterType = DateFilterType.THIS_MONTH,
    val label: String = ""
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
                today.toDateRange()
            }

            DateFilterType.YESTERDAY -> {
                today.yesterday.toDateRange()
            }

            DateFilterType.TOMORROW -> {
                today.tomorrow.toDateRange()
            }

            DateFilterType.THIS_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7 // شنبه شروع هفته
                val start = today.getDateByDiff(-diffToStart)
                val end = start.getDateByDiff(6)
                start.toDateRange(end)
            }

            DateFilterType.LAST_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7
                val start = today.getDateByDiff(-diffToStart - 7)
                val end = start.getDateByDiff(6)
                start.toDateRange(end)
            }

            DateFilterType.NEXT_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7
                val start = today.getDateByDiff(-diffToStart + 7)
                val end = start.getDateByDiff(6)
                start.toDateRange(end)
            }

            DateFilterType.THIS_MONTH -> {
                val start = JalaliCalendar(today.year, today.month, 1)
                val end = JalaliCalendar(today.year, today.month, today.getMonthLength())
                start.toDateRange(end)
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
                start.toDateRange(end)
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
                start.toDateRange(end)
            }

            DateFilterType.CUSTOM_RANGE -> {
                if (customFrom != null && customTo != null) {
                    DateRange(customFrom, customTo)
                } else null
            }
        }
    }


    fun getDateRangeText(
        start: Long,
        end: Long,
        filterType: DateFilterType
    ): String {
        val startDate = Date(start)
        val endDate = Date(end)

        val startJalali = JalaliCalendar(startDate)
        val endJalali = JalaliCalendar(endDate)

        return when (filterType) {

            // ===== روزها =====
            DateFilterType.TODAY -> "امروز"
            DateFilterType.YESTERDAY -> "دیروز"
            DateFilterType.TOMORROW -> "فردا"

            // ===== هفته‌ها =====
            DateFilterType.THIS_WEEK -> "این هفته"
            DateFilterType.LAST_WEEK -> "هفته قبل"
            DateFilterType.NEXT_WEEK -> "هفته بعد"

            // ===== ماه‌ها =====
            DateFilterType.THIS_MONTH -> "این ماه"
            DateFilterType.LAST_MONTH -> "ماه قبل"
            DateFilterType.NEXT_MONTH -> "ماه بعد"

            // ===== سایر موارد =====
            else -> when {
                // اگر بازه فقط یک روز بود (start = end)
                startJalali == endJalali -> {
                    "${startJalali.day} ${startJalali.monthString} ${startJalali.year}"
                }

                // اگر بازه از نوع هفته بود (غیر از سه حالت خاص بالا)
                filterType.name.contains("WEEK", ignoreCase = true) -> {
                    val startText = "${startJalali.day} ${startJalali.monthString}"
                    val endText = "${endJalali.day} ${endJalali.monthString}"
                    "$startText تا $endText"
                }

                // اگر بازه از نوع ماه بود (غیر از سه حالت خاص بالا)
                filterType.name.contains("MONTH", ignoreCase = true) -> {
                    "${startJalali.monthString} ${startJalali.year}"
                }

                // حالت بازه‌ی انتخابی یا سایر فیلترها
                else -> {
                    val startText =
                        "${startJalali.day} ${startJalali.monthString} ${startJalali.year}"
                    val endText = "${endJalali.day} ${endJalali.monthString} ${endJalali.year}"
                    "$startText تا $endText"
                }
            }
        }
    }


    fun shiftDateRange(
        start: Long,
        end: Long,
        filterType: DateFilterType,
        direction: Direction
    ): DateRange {

        val diff = if (direction == Direction.NEXT) 1 else -1

        val startJalali = JalaliCalendar(Date(start))
        val endJalali = JalaliCalendar(Date(end))
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

                // حالا بررسی می‌کنیم روز جدید نسبت به امروز کجاست:
                when {
                    newStart == today -> newFilter = DateFilterType.TODAY
                    newStart == today.getYesterday() -> newFilter = DateFilterType.YESTERDAY
                    newStart == today.getTomorrow() -> newFilter = DateFilterType.TOMORROW
                    else -> newFilter = DateFilterType.CUSTOM_RANGE // دیگه روز خاصی نیست
                }
            }

            // ===== هفته‌ها =====
            DateFilterType.THIS_WEEK,
            DateFilterType.LAST_WEEK,
            DateFilterType.NEXT_WEEK -> {
                newStart = startJalali.getDateByDiff(diff * 7)
                newEnd = endJalali.getDateByDiff(diff * 7)

                val thisWeekStart = today.getDateByDiff(-(today.getDayOfWeek() - Calendar.SATURDAY))
                val thisWeekEnd = thisWeekStart.getDateByDiff(6)

                when {
                    newStart == thisWeekStart -> newFilter = DateFilterType.THIS_WEEK
                    newStart.getDateByDiff(7) == thisWeekStart -> newFilter =
                        DateFilterType.LAST_WEEK

                    newStart.getDateByDiff(-7) == thisWeekStart -> newFilter =
                        DateFilterType.NEXT_WEEK

                    else -> newFilter = DateFilterType.CUSTOM_RANGE
                }
            }

            // ===== ماه‌ها =====
            DateFilterType.THIS_MONTH,
            DateFilterType.LAST_MONTH,
            DateFilterType.NEXT_MONTH -> {
                val newMonth = startJalali.getMonth() + diff
                val newYear =
                    startJalali.getYear() + (if (newMonth > 12) 1 else if (newMonth < 1) -1 else 0)
                val adjustedMonth = when {
                    newMonth > 12 -> 1
                    newMonth < 1 -> 12
                    else -> newMonth
                }

                newStart = JalaliCalendar(newYear, adjustedMonth, 1)
                newEnd = JalaliCalendar(newYear, adjustedMonth, newStart.getMonthLength())

                when {
                    newYear == today.getYear() && adjustedMonth == today.getMonth() ->
                        newFilter = DateFilterType.THIS_MONTH

                    newYear == today.getYear() && adjustedMonth == today.getMonth() - 1 ->
                        newFilter = DateFilterType.LAST_MONTH

                    newYear == today.getYear() && adjustedMonth == today.getMonth() + 1 ->
                        newFilter = DateFilterType.NEXT_MONTH

                    else -> newFilter = DateFilterType.CUSTOM_RANGE
                }
            }

            // ===== سایر بازه‌ها =====
            else -> {
                val daysBetween = ((end - start) / (1000 * 60 * 60 * 24)).toInt()
                newStart = startJalali.getDateByDiff(diff * (daysBetween + 1))
                newEnd = endJalali.getDateByDiff(diff * (daysBetween + 1))
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


    fun getPreviousFilter(current: DateFilterType): DateFilterType {
        return when (current) {
            DateFilterType.TODAY -> DateFilterType.YESTERDAY
            DateFilterType.YESTERDAY -> DateFilterType.YESTERDAY // یا یه فیلتر جدید مثلاً "TWO_DAYS_AGO"
            DateFilterType.TOMORROW -> DateFilterType.TODAY

            DateFilterType.THIS_WEEK -> DateFilterType.LAST_WEEK
            DateFilterType.LAST_WEEK -> DateFilterType.LAST_WEEK // یا "TWO_WEEKS_AGO"
            DateFilterType.NEXT_WEEK -> DateFilterType.THIS_WEEK

            DateFilterType.THIS_MONTH -> DateFilterType.LAST_MONTH
            DateFilterType.LAST_MONTH -> DateFilterType.LAST_MONTH // یا "TWO_MONTHS_AGO"
            DateFilterType.NEXT_MONTH -> DateFilterType.THIS_MONTH

            DateFilterType.CUSTOM_RANGE -> DateFilterType.CUSTOM_RANGE
        }
    }

    fun getNextFilter(current: DateFilterType): DateFilterType {
        return when (current) {
            DateFilterType.TODAY -> DateFilterType.TOMORROW
            DateFilterType.YESTERDAY -> DateFilterType.TODAY
            DateFilterType.TOMORROW -> DateFilterType.TOMORROW // یا "DAY_AFTER_TOMORROW"

            DateFilterType.THIS_WEEK -> DateFilterType.NEXT_WEEK
            DateFilterType.LAST_WEEK -> DateFilterType.THIS_WEEK
            DateFilterType.NEXT_WEEK -> DateFilterType.NEXT_WEEK // یا "TWO_WEEKS_LATER"

            DateFilterType.THIS_MONTH -> DateFilterType.NEXT_MONTH
            DateFilterType.LAST_MONTH -> DateFilterType.THIS_MONTH
            DateFilterType.NEXT_MONTH -> DateFilterType.NEXT_MONTH // یا "TWO_MONTHS_LATER"

            DateFilterType.CUSTOM_RANGE -> DateFilterType.CUSTOM_RANGE
        }
    }

    fun getShiftedRange(
        type: DateFilterType,
        offset: Int
    ): DateRange? {
        val today = JalaliCalendar()

        return when (type) {

            DateFilterType.TODAY,
            DateFilterType.YESTERDAY,
            DateFilterType.TOMORROW -> {
                val shifted = today.getDateByDiff(offset)
                shifted.toDateRange()
            }

            DateFilterType.THIS_WEEK,
            DateFilterType.LAST_WEEK,
            DateFilterType.NEXT_WEEK -> {
                val dayOfWeek = today.dayOfWeek
                val diffToStart = (dayOfWeek - Calendar.SATURDAY + 7) % 7
                val start = today.getDateByDiff(-diffToStart + offset * 7)
                val end = start.getDateByDiff(6)
                start.toDateRange(end)
            }

            DateFilterType.THIS_MONTH,
            DateFilterType.LAST_MONTH,
            DateFilterType.NEXT_MONTH -> {
                var year = today.year
                var month = today.month + offset
                while (month > 12) {
                    month -= 12; year++
                }
                while (month < 1) {
                    month += 12; year--
                }
                val start = JalaliCalendar(year, month, 1)
                val end = JalaliCalendar(year, month, start.getMonthLength())
                start.toDateRange(end)
            }

            DateFilterType.CUSTOM_RANGE -> null
        }
    }

    fun getFilterDisplayText(type: DateFilterType, offset: Int): String {
        val range = getShiftedRange(type, offset)
        return when (type) {
            DateFilterType.TODAY,
            DateFilterType.YESTERDAY,
            DateFilterType.TOMORROW -> {
                range?.let { "${it.startFormatted()}" } ?: ""
            }

            DateFilterType.THIS_WEEK,
            DateFilterType.LAST_WEEK,
            DateFilterType.NEXT_WEEK -> {
                range?.let { "${it.startFormatted()} تا ${it.endFormatted()}" } ?: ""
            }

            DateFilterType.THIS_MONTH,
            DateFilterType.LAST_MONTH,
            DateFilterType.NEXT_MONTH -> {
                range?.let { "${it.startMonthName()} ${it.startYear()}" } ?: ""
            }

            DateFilterType.CUSTOM_RANGE -> "بازه انتخابی"
        }
    }

    fun DateRange.startFormatted(): String {
        val calendar = JalaliCalendar().fromTimestamp(start)
        return calendar.formatAsShortDate()
    }

    fun DateRange.endFormatted(): String {
        val calendar = JalaliCalendar().fromTimestamp(end)
        return calendar.formatAsShortDate()
    }

    fun DateRange.startMonthName(): String {
        val calendar = JalaliCalendar().fromTimestamp(start)
        return calendar.monthString
    }

    fun DateRange.startYear(): Int {
        val calendar = JalaliCalendar().fromTimestamp(start)
        return calendar.year
    }


    fun JalaliCalendar.formatAsShortDate(): String {
        return "${this.day} ${this.monthString} ${this.year}"
    }


    fun JalaliCalendar.fromTimestamp(ts: Long): JalaliCalendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = ts
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1 // چون Calendar.MONTH از 0 شروع می‌شه
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return JalaliCalendar(year, month, day)
    }

    /**
     * تبدیل تاریخ شمسی به بازه‌ی زمانی میلادی برحسب timestamp
     */
    private fun JalaliCalendar.toDateRange(): DateRange {
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

        return DateRange(start, end)
    }

    /**
     * بازه بین دو تاریخ شمسی (شروع تا پایان)
     */
    private fun JalaliCalendar.toDateRange(endDate: JalaliCalendar): DateRange {
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
        return DateRange(startGc.timeInMillis, endGc.timeInMillis)
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