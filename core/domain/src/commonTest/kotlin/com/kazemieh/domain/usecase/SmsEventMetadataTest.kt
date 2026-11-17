package com.kazemieh.domain.usecase

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import kotlinx.datetime.TimeZone
import kotlin.test.*
import kotlin.time.Instant

class SmsEventMetadataTest {
    private val zone = TimeZone.of("Asia/Tehran")
    private val received = PersianDateTime(1405, 8, 26, 14, 30).toEpochMilliseconds(zone)
    private val draft = SmsDraft(sender = "989100001234", body = "برداشت مبلغ 120,000 ریال",
        amount = 120_000, bankName = "Unknown", type = TransactionType.EXPENSE, timeStamp = received, date = "")

    @Test fun jalaliBankTimePreservesSeconds() {
        assertEquals(PersianDateTime(1405, 8, 25, 12, 25, 42).toEpochMilliseconds(zone),
            smsTransactionTimestamp("بانک تست\n1405/08/25 12:25:42 برداشت", received, zone))
    }
    @Test fun persianDigitsAndNewlineTimeAreAccepted() {
        assertEquals(PersianDateTime(1405, 8, 25, 12, 25).toEpochMilliseconds(zone),
            smsTransactionTimestamp("۱۴۰۵/۰۸/۲۵\n۱۲:۲۵", received, zone))
    }
    @Test fun gregorianTimeUsesExplicitZone() {
        assertEquals(Instant.parse("2026-11-16T08:55:42Z").toEpochMilliseconds(),
            smsTransactionTimestamp("2026-11-16T12:25:42", received, zone))
    }
    @Test fun missingIncompleteInvalidAndFutureTimesUseReceipt() {
        for (body in listOf("برداشت", "1405/08/25", "12:25", "1405/13/25 12:25",
            "1405/08/25 25:00", "1405/08/27 12:25", "2026/02/30 12:25")) {
            assertEquals(received, smsTransactionTimestamp(body, received, zone), body)
        }
    }
    @Test fun smallBankClockSkewIsAccepted() {
        assertEquals(received + 120_000, smsTransactionTimestamp("1405/08/26 14:32", received, zone))
    }
    @Test fun personalFinancialTextIsNotEnough() {
        assertFalse(isBankSmsCandidate(draft, emptyList(), false))
        assertFalse(isBankSmsCandidate(draft.copy(body = "تکمیلی برداشت مبلغ 120,000 ریال", bankName = "Melli"), emptyList(), false))
    }
    @Test fun knownAndUnknownBankHeadersAreAccepted() {
        for (header in listOf("بانک تست", "بلوبانک", "Bank Example", "ملی")) {
            assertTrue(isBankSmsCandidate(draft.copy(body = "$header\n${draft.body}"), emptyList(), false), header)
        }
    }
    @Test fun registeredOrManuallyConfiguredSenderDoesNotNeedHeader() {
        assertTrue(isBankSmsCandidate(draft, emptyList(), true))
        val source = Source(id = 1, name = "Custom", colorId = 0, iconId = 0, smsSender = "۹۸۹۱۰۰۰۰۱۲۳۴")
        assertTrue(isBankSmsCandidate(draft, listOf(source), false))
        assertFalse(isBankSmsCandidate(draft.copy(sender = "someoneElse"), listOf(source), false))
    }
}
