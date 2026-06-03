package com.kazemieh.common.persiandatetime

import com.kazemieh.common.persiandatetime.domain.IDateConverter
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

/**
 * الگوریتم استاندارد تبدیل تقویم جلالی به گریگوری (بر اساس الگوریتم Khawarizm)
 */
internal object PersianDateConverterImpl : IDateConverter<PersianDateTime> {

    override fun fromGregorian(date: LocalDate): PersianDateTime {
        val gy = date.year
        val gm = date.month.number
        val gd = date.day
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (isGregorianLeap(gy)) gDaysInMonth[2] = 29

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) gDayNo += gDaysInMonth[i + 1]
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + jDayNo % 31
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + (jDayNo - 186) % 30
        }

        return PersianDateTime(jy, jm, jd)
    }

    override fun toGregorian(date: PersianDateTime): LocalDateTime {
        val jy = date.year - 979
        val jm = date.month - 1
        val jd = date.day - 1

        var jDayNo = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4
        if (jm < 6) jDayNo += jm * 31
        else jDayNo += jm * 30 + 6
        jDayNo += jd

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) gDayNo++
            else leap = false
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        var gd = gDayNo + 1
        val gDaysInMonth = intArrayOf(0, 31, if (isGregorianLeap(gy)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        
        var gm = 1
        while (gm <= 12 && gd > gDaysInMonth[gm]) {
            gd -= gDaysInMonth[gm]
            gm++
        }

        return LocalDateTime(gy, gm, gd, date.hour, date.minute, date.second)
    }

    private fun isGregorianLeap(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
