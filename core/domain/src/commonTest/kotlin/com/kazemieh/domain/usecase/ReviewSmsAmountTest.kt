package com.kazemieh.domain.usecase

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import kotlin.test.*

class ReviewSmsAmountTest {
    private fun draft(amount: Long, body: String) = SmsDraft(sender = "bank", body = body,
        amount = amount, bankName = "Blu", type = TransactionType.EXPENSE, timeStamp = 1, date = "")

    @Test fun canonicalRialDraftDoesNotNeedReview() {
        assertNull(reviewSmsAmount(draft(500_000, "برداشت 500,000 ریال موجودی 90,000 تومان")))
    }
    @Test fun canonicalTomanDraftDoesNotNeedReview() {
        assertNull(reviewSmsAmount(draft(500_000, "برداشت ۵۰٬۰۰۰ تومان موجودی ۹۰۰٬۰۰۰ ریال")))
    }
    @Test fun legacyTomanDraftGetsTheExplicitCanonicalSuggestion() {
        assertEquals(SmsAmountReview(500_000), reviewSmsAmount(draft(50_000,
            "بلوبانک برداشت مبلغ: 50,000 تومان موجودی 900,000 ریال")))
    }
    @Test fun differentStoredRialAmountAlsoNeedsAnExplicitDecision() {
        assertEquals(SmsAmountReview(500_000), reviewSmsAmount(draft(50_000, "برداشت 500,000 ریال")))
    }
    @Test fun messagesWithoutAUnitKeepTheirExistingContract() {
        assertNull(reviewSmsAmount(draft(50_000, "برداشت 50,000 موجودی 900,000")))
    }
    @Test fun aBalanceAloneDoesNotProvideACorrection() {
        assertEquals(SmsAmountReview(null), reviewSmsAmount(draft(50_000, "برداشت انجام شد موجودی 900,000 ریال")))
    }
    @Test fun overflowingExplicitTomanRequiresManualReviewWithoutASuggestion() {
        assertEquals(SmsAmountReview(null), reviewSmsAmount(draft(50_000,
            "برداشت 922337203685477581 تومان موجودی 900,000 ریال")))
    }
    @Test fun colloquialUnitIsReviewedUsingTheTransactionNotTheBalance() {
        assertEquals(SmsAmountReview(500_000), reviewSmsAmount(draft(50_000,
            "برداشت ۵۰٬۰۰۰ تومن موجودی ۹۰۰٬۰۰۰ ریال")))
    }
}
