package com.kazemieh.common

import ir.huri.jcal.JalaliCalendar
import java.util.Calendar

data class DateRange(
    val fromTimestamp: Long,
    val toTimestamp: Long
)

object DateFilterHelper {

    fun getRangeG(
        type: DateFilterType,
        customFrom: Long? = null,
        customTo: Long? = null
    ): DateRange? {
        val calendar = Calendar.getInstance()

        return when (type) {
            DateFilterType.TODAY -> {
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.TOMORROW -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    add(Calendar.DAY_OF_WEEK, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.LAST_WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    add(Calendar.DAY_OF_WEEK, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.NEXT_WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    add(Calendar.DAY_OF_WEEK, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.NEXT_MONTH -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val end = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                DateRange(start, end)
            }

            DateFilterType.CUSTOM_RANGE -> {
                if (customFrom != null && customTo != null) {
                    DateRange(customFrom, customTo)
                } else null
            }
        }
    }

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