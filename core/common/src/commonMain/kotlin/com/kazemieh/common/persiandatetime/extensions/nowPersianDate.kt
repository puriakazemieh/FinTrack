package com.kazemieh.common.persiandatetime.extensions

import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.domain.PersianMonth
import com.kazemieh.common.persiandatetime.domain.PersianWeekday
import com.kazemieh.common.persiandatetime.util.Main
import com.kazemieh.common.persiandatetime.util.PersianDateTimeFormat
import com.kazemieh.common.persiandatetime.util.PersianLeap
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
        /**
         * Returns the current time as a PersianDateTime in the given [timeZone].
         *
         * Usage:
         * ```kotlin
         * val clock = Clock.System
         * val persianNow = clock.nowPersianDate(TimeZone.of("Asia/Tehran"))
         * println(persianNow.toDateTimeString())
         * ```
         */
fun Clock.nowPersianDate(timeZone: TimeZone): PersianDateTime =
    this.now().toPersianDateTime(timeZone)

@OptIn(ExperimentalTime::class)
        /**
         * Returns the current time as a PersianDateTime in the Tehran time zone.
         *
         * Usage:
         * ```kotlin
         * val clock = Clock.System
         * val tehranNow = clock.nowInTehran
         * println(tehranNow.toDateTimeString())
         * ```
         */
val Clock.nowInTehran: PersianDateTime
    get() = this.nowPersianDate(TimeZone.of("Asia/Tehran"))

/**
 * Converts a PersianDateTime to a LocalDate.
 *
 * Usage:
 * ```kotlin
 * val persianDate = PersianDateTime(1402, 7, 15, 14, 30, 0)
 * val localDate: LocalDate = persianDate.toLocalDate()
 * ```
 */
fun PersianDateTime.toLocalDate(): LocalDate = Main.persianDateConverter.toGregorian(this).date


@OptIn(ExperimentalTime::class)
fun PersianDateTime.toInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant =
    this.toLocalDateTime().toInstant(timeZone)

@OptIn(ExperimentalTime::class)
fun PersianDateTime.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    this.toLocalDateTime().toInstant(timeZone).toEpochMilliseconds()

/**
 * Converts a PersianDateTime to a LocalDateTime.
 *
 * Usage:
 * ```kotlin
 * val persianDate = PersianDateTime(1402, 7, 15, 14, 30, 0)
 * val localDateTime: LocalDateTime = persianDate.toLocalDateTime()
 * ```
 */
fun PersianDateTime.toLocalDateTime(): LocalDateTime = Main.persianDateConverter.toGregorian(this)

/**
 * Subtracts a given amount of [dateTimeUnit] from this [PersianDateTime].
 *
 * @param value The amount of units to subtract.
 * @param dateTimeUnit The date-based unit (e.g. day, month, year) to subtract.
 * @return A new [PersianDateTime] with the adjusted date.
 */
fun PersianDateTime.minus(value: Int, dateTimeUnit: DateTimeUnit.DateBased): PersianDateTime {
    val dateTime = this
    val localDate = this.toLocalDate().minus(value, dateTimeUnit)
    return localDate.toPersianDateTime(dateTime.hour, dateTime.minute, dateTime.second)
}

/**
 * Subtracts a [DatePeriod] (e.g. years, months, days) from this [PersianDateTime].
 *
 * @param period The [DatePeriod] to subtract.
 * @return A new [PersianDateTime] with the adjusted date.
 */
fun PersianDateTime.minus(period: DatePeriod): PersianDateTime {
    val dateTime = this
    val localDate = this.toLocalDate().minus(period)
    return localDate.toPersianDateTime(dateTime.hour, dateTime.minute, dateTime.second)
}

/**
 * Calculates the [DatePeriod] difference between this [PersianDateTime] and another [PersianDateTime].
 *
 * @param other The other [PersianDateTime] to subtract.
 * @return The [DatePeriod] representing the difference.
 */
fun PersianDateTime.minus(other: PersianDateTime): DatePeriod =
    this.toLocalDate().minus(other.toLocalDate())

/**
 * Adds a given amount of [dateTimeUnit] to this [PersianDateTime].
 *
 * @param value The amount of units to add.
 * @param dateTimeUnit The date-based unit (e.g. day, month, year) to add.
 * @return A new [PersianDateTime] with the adjusted date.
 */
fun PersianDateTime.plus(value: Int, dateTimeUnit: DateTimeUnit.DateBased): PersianDateTime {
    val dateTime = this
    val localDate = this.toLocalDate().plus(value, dateTimeUnit)
    return localDate.toPersianDateTime(dateTime.hour, dateTime.minute, dateTime.second)
}

/**
 * Adds a [DatePeriod] (e.g. years, months, days) to this [PersianDateTime].
 *
 * @param period The [DatePeriod] to add.
 * @return A new [PersianDateTime] with the adjusted date.
 */
fun PersianDateTime.plus(period: DatePeriod): PersianDateTime {
    val dateTime = this
    val localDate = this.toLocalDate().plus(period)
    return localDate.toPersianDateTime(dateTime.hour, dateTime.minute, dateTime.second)
}

/**
 * Adds a number of days to this [PersianDateTime].
 *
 * @param days The number of days to add (can be negative).
 * @return A new [PersianDateTime] with the adjusted date.
 *
 * @deprecated Use [plus] with [DateTimeUnit.DAY] instead.
 * Example: `date.plus(days, DateTimeUnit.DAY)`
 */
@Deprecated(
    message = "Use plus(days, DateTimeUnit.DAY) instead",
    replaceWith = ReplaceWith("this.plus(days, DateTimeUnit.DAY)")
)
fun PersianDateTime.plusDays(days: Int): PersianDateTime {
    val localDateTime = this.toLocalDateTime()
    val newDate = localDateTime.date.plus(days, DateTimeUnit.DAY)
    return newDate.toPersianDateTime(this.hour, this.minute, this.second)
}

/**
 * Subtracts a number of days from this [PersianDateTime].
 *
 * @param days The number of days to subtract.
 * @return A new [PersianDateTime] with the adjusted date.
 *
 * @deprecated Use [minus] with [DateTimeUnit.DAY] instead.
 * Example: `date.minus(days, DateTimeUnit.DAY)`
 */
@Deprecated(
    message = "Use minus(days, DateTimeUnit.DAY) instead",
    replaceWith = ReplaceWith("this.minus(days, DateTimeUnit.DAY)")
)
fun PersianDateTime.minusDays(days: Int): PersianDateTime =
    plusDays(-days)


/**
 * Checks if this PersianDateTime is before [other].
 *
 * Usage:
 * ```kotlin
 * val result = persianDate.isBefore(otherDate)
 * ```
 */
fun PersianDateTime.isBefore(other: PersianDateTime) =
    this.toLocalDateTime() < other.toLocalDateTime()

/**
 * Checks if this PersianDateTime is after [other].
 *
 * Usage:
 * ```kotlin
 * val result = persianDate.isAfter(otherDate)
 * ```
 */

fun PersianDateTime.isAfter(other: PersianDateTime) =
    this.toLocalDateTime() > other.toLocalDateTime()

/**
 * Checks if this PersianDateTime is between [start] and [end].
 *
 * Usage:
 * ```kotlin
 * val result = persianDate.isBetween(startDate, endDate)
 * ```
 */
fun PersianDateTime.isBetween(start: PersianDateTime, end: PersianDateTime) =
    this.toLocalDateTime() >= start.toLocalDateTime() && this.toLocalDateTime() <= end.toLocalDateTime()

/**
 * Checks if the year of this PersianDateTime is a leap year.
 *
 * Usage:
 * ```kotlin
 * val isLeap = persianDate.isLeapYear()
 * ```
 */
fun PersianDateTime.isLeapYear(): Boolean = PersianLeap.getLeapFactor(year) == 0

/**
 * Returns the number of days in the month of this PersianDateTime.
 *
 * Usage:
 * ```kotlin
 * val length = persianDate.monthLength()
 * ```
 */
fun PersianDateTime.monthLength(): Int = when {
    month < 7 -> 31
    month < 12 -> 30
    month == 12 -> if (this.isLeapYear()) 30 else 29
    else -> 0
}

/**
 * Returns the month of this PersianDateTime.
 *
 * Usage:
 * ```kotlin
 * val name = persianDate.persianMonth()
 * ```
 */
fun PersianDateTime.persianMonth(): PersianMonth = PersianMonth.entries.toTypedArray()
    .getOrElse(month) { PersianMonth.UNKNOWN }

/**
 * Returns the Persian day of the week.
 *
 * Usage:
 * ```kotlin
 * val weekday = persianDate.persianDayOfWeek()
 * ```
 */
fun PersianDateTime.persianDayOfWeek(): PersianWeekday =
    PersianWeekday.fromNumber(this.dayOfWeekIndex + 1)

/**
 * Returns the 0-indexed day of week starting from Saturday (0 = Saturday, 6 = Friday).
 */
val PersianDateTime.dayOfWeekIndex: Int
    get() = (this.toLocalDate().dayOfWeek.isoDayNumber + 1) % 7

/**
 * Formats this PersianDateTime using the DSL builder.
 *
 * Usage:
 * ```kotlin
 * val formatted = persianDate.format {
 *     day = true
 *     monthName = true
 *     year = true
 *     hour = true
 *     minute = true
 * }
 * ```
 */
fun PersianDateTime.format(builderAction: PersianDateTimeFormat.() -> Unit): String {
    val builder = PersianDateTimeFormat()
    builder.dateTime = this
    builder.builderAction()
    return builder.build()
}

/**
 * Returns a date string in `YYYY/MM/DD` format.
 *
 * Usage:
 * ```kotlin
 * val dateStr = persianDate.toDateString()
 * ```
 */
fun PersianDateTime.toDateString(): String =
    "${year.toString().padStart(4, '0')}/${month.toString().padStart(2, '0')}/${
        day.toString().padStart(2, '0')
    }"

/**
 * Returns a time string in `HH:MM:SS` format.
 *
 * Usage:
 * ```kotlin
 * val timeStr = persianDate.toTimeString()
 * ```
 */
fun PersianDateTime.toTimeString(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${
        second.toString().padStart(2, '0')
    }"

/**
 * Returns a datetime string in `YYYY/MM/DD HH:MM:SS` format.
 *
 * Usage:
 * ```kotlin
 * val dateTimeStr = persianDate.toDateTimeString()
 * ```
 */
fun PersianDateTime.toDateTimeString(): String = "${toDateString()} ${toTimeString()}"


@OptIn(ExperimentalTime::class)
        /**
         * Converts this [Instant] to a [PersianDateTime] in the given [timeZone].
         *
         * Usage:
         * ```kotlin
         * val nowInstant = Clock.System.now()
         * val persianDate = nowInstant.toPersianDateTime(TimeZone.of("Asia/Tehran"))
         * println(persianDate.toDateTimeString())
         * ```
         *
         * @param timeZone The time zone to use for conversion.
         * @return The corresponding PersianDateTime.
         */
fun Instant.toPersianDateTime(timeZone: TimeZone): PersianDateTime =
    Main.persianDateConverter.fromGregorian(this.toLocalDateTime(timeZone).date)


/**
 * Converts this [LocalDate] to a [PersianDateTime], optionally setting the time.
 *
 * Usage:
 * ```kotlin
 * val localDate = LocalDate(2023, 9, 28)
 * val persianDate = localDate.toPersianDateTime(hour = 14, minute = 30)
 * println(persianDate.toDateTimeString())
 * ```
 *
 * @param hour The hour to set in the resulting PersianDateTime (default is 0).
 * @param minute The minute to set in the resulting PersianDateTime (default is 0).
 * @param second The second to set in the resulting PersianDateTime (default is 0).
 * @return The corresponding PersianDateTime with the specified time.
 */
fun LocalDate.toPersianDateTime(hour: Int = 0, minute: Int = 0, second: Int = 0): PersianDateTime {
    val date = Main.persianDateConverter.fromGregorian(this)
    return date.copy(hour = hour, minute = minute, second = second)
}


/**
 * Converts this [LocalDateTime] to a [PersianDateTime], preserving the time components.
 *
 * Usage:
 * ```kotlin
 * val localDateTime = LocalDateTime(2023, 9, 28, 14, 30, 0)
 * val persianDate = localDateTime.toPersianDateTime()
 * println(persianDate.toDateTimeString())
 * ```
 *
 * @return The corresponding PersianDateTime with the same hour, minute, and second.
 */
fun LocalDateTime.toPersianDateTime(): PersianDateTime {
    val date = Main.persianDateConverter.fromGregorian(this.date)
    return date.copy(hour = hour, minute = minute, second = second)
}