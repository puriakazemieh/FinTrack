package com.kazemieh.designsystem.component.jalali

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * مدل تاریخ شمسی (Jalali) – کاملاً مولتی‌پلتفرم و بدون وابستگی به JVM
 * @param year سال شمسی (مثلاً ۱۴۰۳)
 * @param month ماه شمسی (۱ تا ۱۲)
 * @param day روز شمسی (۱ تا ۳۱)
 */
class JalaliCalendar(val year: Int, val month: Int, val day: Int) {

    constructor() : this(
        today().year,
        today().month,
        today().day
    )
    // ---------- توابع جانبی ----------
    val monthLength: Int
        get() = when (month) {
            1, 2, 3, 4, 5, 6 -> 31
            7, 8, 9, 10, 11 -> 30
            12 -> if (isLeapYear()) 30 else 29
            else -> 0
        }

    val monthString: String
        get() = when (month) {
            1 -> "فروردین"
            2 -> "اردیبهشت"
            3 -> "خرداد"
            4 -> "تیر"
            5 -> "مرداد"
            6 -> "شهریور"
            7 -> "مهر"
            8 -> "آبان"
            9 -> "آذر"
            10 -> "دی"
            11 -> "بهمن"
            12 -> "اسفند"
            else -> ""
        }

    val dayOfWeek: Int
        get() {
            // تبدیل به Gregorian و گرفتن day of week (۱ = یکشنبه, ۷ = شنبه)
            val (gYear, gMonth, gDay) = toGregorian()
            return gregorianDayOfWeek(gYear, gMonth, gDay)
        }

    val dayOfWeekString: String
        get() = when (dayOfWeek) {
            1 -> "یک‌شنبه"
            2 -> "دوشنبه"
            3 -> "سه‌شنبه"
            4 -> "چهارشنبه"
            5 -> "پنجشنبه"
            6 -> "جمعه"
            7 -> "شنبه"
            else -> ""
        }

    fun isLeapYear(): Boolean = getLeapFactor(year) == 0

    fun toGregorian(): Triple<Int, Int, Int> {
        val julianDay = toJulianDay()
        return julianDayToGregorian(julianDay)
    }

    fun getYesterday(): JalaliCalendar = getDateByDiff(-1)
    fun getTomorrow(): JalaliCalendar = getDateByDiff(1)

    private fun getDateByDiff(diff: Int): JalaliCalendar {
        val (gy, gm, gd) = toGregorian()
        val jd = gregorianToJulianDay(gy, gm, gd) + diff
        val (newGy, newGm, newGd) = julianDayToGregorian(jd)
        return fromGregorian(newGy, newGm, newGd)
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
        return this.toJulianDay() < other.toJulianDay()
    }

    fun isAfter(other: JalaliCalendar): Boolean {
        return this.toJulianDay() > other.toJulianDay()
    }

    fun isEqual(other: JalaliCalendar): Boolean {
        return this.toJulianDay() == other.toJulianDay()
    }

    fun isBeforeOrEqual(other: JalaliCalendar): Boolean {
        return this.toJulianDay() <= other.toJulianDay()
    }

    fun isAfterOrEqual(other: JalaliCalendar): Boolean {
        return this.toJulianDay() >= other.toJulianDay()
    }

    fun toTimestamp(): Long {
        val (year, month, day) = toGregorian()
        return gregorianToTimestamp(year, month, day)
    }

    // ---------- همراه شیء (companion object) ----------
    companion object {
        /**
         * تاریخ امروز (با استفاده از kotlinx-datetime)
         */
        fun today(): JalaliCalendar {
            val now = kotlin.time.Clock.System.now()
            val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            return fromGregorian(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        }

        /**
         * ساخت شیء از تاریخ میلادی
         */
        fun fromGregorian(year: Int, month: Int, day: Int): JalaliCalendar {
            val jd = gregorianToJulianDay(year, month, day)
            return fromJulianDay(jd)
        }

        // ---------- توابع خصوصی محاسباتی ----------
        private fun gregorianToJulianDay(y: Int, m: Int, d: Int): Int {
            var year = y
            var month = m
            if (month < 3) {
                year -= 1
                month += 12
            }
            val a = year / 100
            val b = 2 - a + a / 4
            return ((1461 * (year + 4800)) / 4
                    + (367 * (month - 2)) / 12
                    - (3 * ((year + 4900) / 100)) / 4
                    + d - 32075) + b
        }

        private fun julianDayToGregorian(jd: Int): Triple<Int, Int, Int> {
            var j = jd + 32044
            val g = j / 146097
            val dg = j % 146097
            val c = (dg / 36524 + 1) * 3 / 4
            j = j + c
            val a = (j * 4 + 3) / 146097
            val b = j - (146097 * a) / 4
            val year = (b * 4 + 3) / 1461
            val month = (b - (1461 * year) / 4) * 5 / 153
            val day = b - (1461 * year) / 4 - (153 * month) / 5 + 1
            val finalYear = 100 * a + year - 4800 + (month + 2) / 12
            val finalMonth = (month + 2) % 12 + 1
            return Triple(finalYear, finalMonth, day)
        }

        private fun gregorianDayOfWeek(year: Int, month: Int, day: Int): Int {
            var y = year
            var m = month
            if (m < 3) {
                y -= 1
                m += 12
            }
            val k = y % 100
            val j = y / 100
            val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
            // h=0 -> شنبه, 1-> یکشنبه, ... 6-> جمعه
            return when (h) {
                0 -> 7   // شنبه
                1 -> 1   // یکشنبه
                2 -> 2
                3 -> 3
                4 -> 4
                5 -> 5
                6 -> 6   // جمعه
                else -> 1
            }
        }

        private fun getLeapFactor(jalaliYear: Int): Int {
            val breaks = intArrayOf(
                -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
                1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
            )
            var jp = breaks[0]
            for (j in 1..19) {
                val jm = breaks[j]
                val jump = jm - jp
                if (jalaliYear < jm) {
                    var N = jalaliYear - jp
                    if (jump - N < 6)
                        N = N - jump + (jump + 4) / 33 * 33
                    val leap = ((((N + 1) % 33) - 1) % 4)
                    return if (leap == -1) 4 else leap
                }
                jp = jm
            }
            return 0
        }

        private fun fromJulianDay(julianDay: Int): JalaliCalendar {
            val (gy, gm, gd) = julianDayToGregorian(julianDay)
            var jalaliYear = gy - 621
            val gregorianFirstFarvardin = getGregorianFirstFarvardin(jalaliYear)
            val jdFirstFarvardin = gregorianToJulianDay(
                gregorianFirstFarvardin.first,
                gregorianFirstFarvardin.second,
                gregorianFirstFarvardin.third
            )
            var diff = julianDay - jdFirstFarvardin
            var jalaliMonth: Int
            var jalaliDay: Int
            if (diff >= 0) {
                if (diff <= 185) {
                    jalaliMonth = 1 + diff / 31
                    jalaliDay = (diff % 31) + 1
                    return JalaliCalendar(jalaliYear, jalaliMonth, jalaliDay)
                } else {
                    diff -= 186
                }
            } else {
                diff += 179
                if (getLeapFactor(jalaliYear) == 1) diff++
                jalaliYear--
            }
            jalaliMonth = 7 + diff / 30
            jalaliDay = (diff % 30) + 1
            return JalaliCalendar(jalaliYear, jalaliMonth, jalaliDay)
        }

        private fun getGregorianFirstFarvardin(jalaliYear: Int): Triple<Int, Int, Int> {
            val breaks = intArrayOf(
                -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
                1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
            )
            var gregorianYear = jalaliYear + 621
            var jalaliLeap = -14
            var jp = breaks[0]
            for (j in 1..19) {
                val jm = breaks[j]
                val jump = jm - jp
                if (jalaliYear < jm) {
                    var N = jalaliYear - jp
                    jalaliLeap += N / 33 * 8 + (N % 33 + 3) / 4
                    if (jump % 33 == 4 && (jump - N) == 4)
                        jalaliLeap++
                    val gregorianLeap = gregorianYear / 4 - (gregorianYear / 100 + 1) * 3 / 4 - 150
                    var marchDay = 20 + (jalaliLeap - gregorianLeap)
                    if ((jump - N) < 6)
                        N = N - jump + (jump + 4) / 33 * 33
                    return Triple(gregorianYear, 3, marchDay)
                }
                jalaliLeap += jump / 33 * 8 + (jump % 33) / 4
                jp = jm
            }
            return Triple(gregorianYear, 3, 20)
        }

        private fun toJulianDay(year: Int, month: Int, day: Int): Int {
            val (gy, gm, gd) = getGregorianFirstFarvardin(year)
            val jdFirst = gregorianToJulianDay(gy, gm, gd)
            val daysBefore = if (month <= 6)
                (month - 1) * 31
            else
                186 + (month - 7) * 30
            return jdFirst + daysBefore + (day - 1)
        }

        private fun gregorianToTimestamp(year: Int, month: Int, day: Int): Long {
            // محاسبه روز ژولین
            var y = year
            var m = month
            if (m < 3) {
                y -= 1
                m += 12
            }
            val a = y / 100
            val b = 2 - a + a / 4
            val julianDay = ((1461 * (y + 4800)) / 4
                    + (367 * (m - 2)) / 12
                    - (3 * ((y + 4900) / 100)) / 4
                    + day - 32075) + b

            // تبدیل روز ژولین به میلی‌ثانیه (از سال 1970)
            // JULIAN_DAY_EPOCH = 2440588 (روز ژولین برای 1970-01-01)
            val JULIAN_DAY_EPOCH = 2440588L
            val millisPerDay = 86400000L
            return (julianDay - JULIAN_DAY_EPOCH) * millisPerDay
        }

        fun fromTimestamp(timestamp: Long): JalaliCalendar {
            val millisPerDay = 86400000L
            val julianDay = (timestamp / millisPerDay) + 2440588L
            return fromJulianDay(julianDay.toInt())
        }

        fun fromEpochMilliseconds(epochMillis: Long): JalaliCalendar {
            val JULIAN_DAY_EPOCH = 2440588L
            val MILLIS_PER_DAY = 86400000L
            val julianDay = (epochMillis / MILLIS_PER_DAY) + JULIAN_DAY_EPOCH
            return fromJulianDay(julianDay.toInt())
        }
    }

    // ---------- توابع نمونه (instance) که از companion استفاده می‌کنند ----------
    private fun toJulianDay(): Int = toJulianDay(year, month, day)
    private fun getGregorianFirstFarvardin(): Triple<Int, Int, Int> =
        getGregorianFirstFarvardin(year)

    private fun getLeapFactor(year: Int): Int = Companion.getLeapFactor(year)
}