package com.kazemieh.domain.usecase

import com.kazemieh.common.model.BankIdentity
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.toEnglishDigits

/** Returns a unique account or leaves the choice to the user. Never guesses the first/default. */
fun resolveSmsSource(draft: SmsDraft, sources: List<Source>): Source? {
    val saved = sources.filter { it.id != null }
    // A persisted selection can be manual. A deleted selection must not silently switch accounts.
    draft.sourceId?.let { id -> return saved.singleOrNull { it.id == id } }
    val bank = BankIdentity.fromName(draft.bankName)
    val senderMatches = saved.filter {
        !it.smsSender.isNullOrBlank() && it.smsSender?.trim()?.toEnglishDigits()
            ?.equals(draft.sender.trim().toEnglishDigits(), ignoreCase = true) == true
    }
    fun unique(candidates: List<Source>): Source? {
        if (candidates.size == 1) return candidates.single()
        val bySender = candidates.filter { it in senderMatches }.ifEmpty { candidates }
        if (bySender.size == 1) return bySender.single()
        return bySender.filter { bank?.matchesSourceName(it.name) == true }.singleOrNull()
    }

    val bodyNumbers = Regex("[0-9]+").findAll(draft.body.toEnglishDigits()).map { it.value }.toSet()
    val fullMatches = saved.filter { source ->
        source.identifiers().any { it.length >= 8 && it in bodyNumbers }
    }
    if (fullMatches.isNotEmpty()) return unique(fullMatches)

    draft.sourceIdentifier?.let { raw ->
        val identifier = raw.digits()
        if (identifier.length < 4) return null
        // An unknown/conflicting identifier is not permission to fall back to a bank name.
        return unique(saved.filter { source -> source.identifiers().any { it.endsWith(identifier) } })
    }
    if (senderMatches.isNotEmpty()) return unique(senderMatches)
    return saved.filter { bank?.matchesSourceName(it.name) == true }.singleOrNull()
}

/** Posting uses the app's active currency; no implicit FX or source-balance conversion exists. */
fun isSmsSourceCurrencyCompatible(source: Source?, activeCurrencyCode: String): Boolean =
    activeCurrencyCode in setOf("IRT", "IRR") && source?.currencyCode == activeCurrencyCode

private fun Source.identifiers(): List<String> = listOfNotNull(cardNumber, accountNumber)
    .map { it.digits() }.filter { it.isNotEmpty() }

private fun String.digits(): String = toEnglishDigits().filter { it in '0'..'9' }
