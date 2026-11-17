package com.kazemieh.domain.usecase

import com.kazemieh.common.model.BankIdentity
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.toEnglishDigits
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** A complete bank timestamp wins; incomplete, invalid or future dates use SMS receipt time. */
fun smsTransactionTimestamp(body: String, receivedAt: Long, zone: TimeZone): Long {
    val match = Regex("(?<![0-9])((?:13|14|20)[0-9]{2})[/-]([0-9]{1,2})[/-]([0-9]{1,2})[ T\\n]+([0-9]{1,2}):([0-9]{2})(?::([0-9]{2}))?(?![0-9])")
        .find(body.toEnglishDigits()) ?: return receivedAt
    val values = match.groupValues.drop(1).map { it.toIntOrNull() ?: 0 }
    val (year, month, day, hour, minute) = values
    val second = values[5]
    val parsed = runCatching {
        if (year < 1500) PersianDateTime(year, month, day, hour, minute, second).toEpochMilliseconds(zone)
        else LocalDateTime(year, month, day, hour, minute, second).toInstant(zone).toEpochMilliseconds()
    }.getOrNull() ?: return receivedAt
    return if (parsed <= receivedAt || parsed - receivedAt <= 300_000L) parsed else receivedAt
}

/** Parser recognition alone is insufficient for a generic personal message containing an amount. */
fun isBankSmsCandidate(draft: SmsDraft, sources: List<Source>, registeredSender: Boolean): Boolean {
    if (registeredSender) return true
    if (sources.any { !it.smsSender.isNullOrBlank() &&
            it.smsSender!!.trim().toEnglishDigits().equals(draft.sender.trim().toEnglishDigits(), true) }) return true
    val header = draft.body.lineSequence().map(String::trim).firstOrNull(String::isNotBlank)
        ?.lowercase()?.replace('ي', 'ی')?.replace('ك', 'ک')
        ?.replace(Regex("[^\\p{L}\\p{N}]+"), " ")?.trim() ?: return false
    return header == "بانک" || header.startsWith("بانک ") || header.startsWith("bank ") ||
        BankIdentity.entries.any { bank -> bank.aliases.any { alias ->
            header == alias || header.startsWith("$alias ")
        } }
}
