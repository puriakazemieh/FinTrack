package com.kazemieh.domain.usecase

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.sms.SmsAmountParser

data class SmsAmountReview(val suggestedAmountInRials: Long?)

/** Detects explicit contradictions without rewriting historical data or guessing a unit. */
fun reviewSmsAmount(draft: SmsDraft): SmsAmountReview? {
    if (!SmsAmountParser.hasExplicitAmount(draft.body)) return null
    val expected = SmsAmountParser.transactionAmountInRials(draft.body, allowUnitless = false)
    return if (expected == draft.amount) null else SmsAmountReview(expected)
}
