package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import kotlin.test.*

class ResolveSmsSourceTest {
    private val a = Source(id = 1, name = "بلو شخصی", colorId = 0, iconId = 0,
        cardNumber = "6219861812341111", accountNumber = "123456781111", smsSender = "bank")
    private val b = a.copy(id = 2, name = "بلو کاری", cardNumber = "6219861912342222", accountNumber = "987654322222")
    private val draft = SmsDraft(sender = "bank", body = "برداشت 500,000 ریال", amount = 500_000,
        bankName = "Blu", type = TransactionType.EXPENSE, timeStamp = 1, date = "")

    @Test fun sharedSenderAndBankAreAmbiguousInEitherOrder() {
        assertNull(resolveSmsSource(draft, listOf(a, b)))
        assertNull(resolveSmsSource(draft, listOf(b, a)))
    }
    @Test fun identifierWinsOverBroadSenderMapping() {
        val expected = b.copy(smsSender = null)
        assertEquals(expected, resolveSmsSource(draft.copy(sourceIdentifier = "2222"), listOf(a, expected)))
    }
    @Test fun persianAndFormattedIdentifiersMatch() {
        val expected = b.copy(cardNumber = "۶۲۱۹-۸۶۱۹-۱۲۳۴-۲۲۲۲", accountNumber = null)
        assertEquals(expected, resolveSmsSource(draft.copy(sourceIdentifier = "۲۲۲۲"), listOf(a, expected)))
    }
    @Test fun unknownIdentifierDoesNotFallBackToUniqueBankOrSender() {
        assertNull(resolveSmsSource(draft.copy(sourceIdentifier = "9999"), listOf(a)))
    }
    @Test fun shortIdentifierIsNotEnoughEvidence() {
        assertNull(resolveSmsSource(draft.copy(sourceIdentifier = "11"), listOf(a)))
    }
    @Test fun matchingSuffixOnTwoAccountsNeedsAChoice() {
        assertNull(resolveSmsSource(draft.copy(sourceIdentifier = "1111"), listOf(a, b.copy(cardNumber = a.cardNumber, accountNumber = null))))
    }
    @Test fun fullCardDisambiguatesSharedSuffix() {
        val second = b.copy(cardNumber = "6219861912341111")
        assertEquals(second, resolveSmsSource(draft.copy(body = "برداشت کارت 6219861912341111", sourceIdentifier = "1111"), listOf(a, second)))
    }
    @Test fun fullAccountCanMatchCustomSourceName() {
        val custom = b.copy(name = "My account", smsSender = null)
        assertEquals(custom, resolveSmsSource(draft.copy(body = "برداشت از حساب ۹۸۷۶۵۴۳۲۲۲۲۲"), listOf(a, custom)))
    }
    @Test fun persistedSelectionIsPreservedEvenWhenBankHintDiffers() {
        val manual = b.copy(name = "Manual", smsSender = null)
        assertEquals(manual, resolveSmsSource(draft.copy(sourceId = 2, sourceIdentifier = "1111"), listOf(a, manual)))
    }
    @Test fun deletedSelectionIsNotReplaced() {
        assertNull(resolveSmsSource(draft.copy(sourceId = 99), listOf(a)))
    }
    @Test fun uniqueConfiguredSenderSupportsUnknownBanks() {
        assertEquals(a, resolveSmsSource(draft.copy(bankName = "Unknown", sender = "BANK"), listOf(a, b.copy(smsSender = null))))
    }
    @Test fun uniqueBankNameSupportsPersianAndEnglish() {
        assertEquals(a, resolveSmsSource(draft.copy(sender = "9891", bankName = "بانک بلو"), listOf(a, b.copy(name = "Mellat Work"))))
        assertEquals(a.copy(name = "Blu Personal"), resolveSmsSource(draft.copy(sender = "9891"), listOf(a.copy(name = "Blu Personal"))))
    }
    @Test fun arbitrarySubstringAndNumericSenderAreNotBankNames() {
        assertNull(resolveSmsSource(draft.copy(sender = "9891", bankName = "ملی"), listOf(a.copy(name = "تکمیلی"))))
        assertNull(resolveSmsSource(draft.copy(sender = "9891", bankName = "9891"), listOf(a.copy(name = "9891"))))
    }
    @Test fun blankIdentifiersDoNotMatchEveryMessage() {
        assertNull(resolveSmsSource(draft.copy(sender = "unknown", bankName = "Unknown"),
            listOf(a.copy(cardNumber = "", accountNumber = "", smsSender = ""))))
    }
    @Test fun bankCatalogNormalizesOldNamesWithoutUiDependencies() {
        assertEquals(BankIdentity.BluBank, BankIdentity.fromName("بلو‌بانک"))
        assertEquals(BankIdentity.Melli, BankIdentity.fromName("ملي"))
        assertEquals(BankIdentity.Mellat, BankIdentity.fromName("Bank Mellat"))
    }
    @Test fun currencyGuardAcceptsOnlyTheMatchingIranianUnit() {
        assertTrue(isSmsSourceCurrencyCompatible(a, "IRT"))
        assertTrue(isSmsSourceCurrencyCompatible(a.copy(currencyCode = "IRR"), "IRR"))
        assertFalse(isSmsSourceCurrencyCompatible(a, "IRR"))
        assertFalse(isSmsSourceCurrencyCompatible(a.copy(currencyCode = "USD"), "IRT"))
        assertFalse(isSmsSourceCurrencyCompatible(a.copy(currencyCode = "USD"), "USD"))
        assertFalse(isSmsSourceCurrencyCompatible(null, "IRT"))
    }
}
