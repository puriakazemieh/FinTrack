package com.kazemieh.domain.usecase

import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class PersianTimestampTest {
    @Test fun instantRoundTripPreservesHoursMinutesAndSecondsInEachZone() {
        val original = PersianDateTime(1405, 8, 24, 16, 37, 42)
        for (zone in listOf(TimeZone.UTC, TimeZone.of("Asia/Tehran"))) {
            val instant = Instant.fromEpochMilliseconds(original.toEpochMilliseconds(zone))
            assertEquals(original, instant.toPersianDateTime(zone), zone.id)
        }
    }
}
