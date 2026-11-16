package com.kazemieh.sms_reader

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SmsAmountTest {
    @BeforeTest
    fun initialize() {
        BankParserRegistry.init(
            listOf(BluParser(), MellatParser(), SaderatParser(), PasargadParser(), SamanParser()),
            GenericBankParser()
        )
    }

    @Test
    fun genericTomanAmountIsStoredInRialsEvenWhenBalanceIsInRials() {
        val draft = assertNotNull(BankParserRegistry.parse("989100001301",
            "بلوبانک برداشت مبلغ: 50,000 تومان موجودی 900,000 ریال"))
        assertEquals(500_000L, draft.amount)
    }

    @Test
    fun eachSpecificParserConvertsTheTransactionUnit() {
        for ((sender, bank) in listOf("blubank" to "بلوبانک", "BankMellat" to "ملت",
            "SaderatBank" to "صادرات", "Pasargad" to "پاسارگاد", "SamanBank" to "سامان")) {
            val draft = assertNotNull(BankParserRegistry.parse(sender,
                "$bank برداشت مبلغ: ۵۰٬۰۰۰ تومان موجودی ۹۰۰٬۰۰۰ ریال"))
            assertEquals(500_000L, draft.amount, sender)
        }
    }

    @Test
    fun rialAmountIsNotConvertedUsingTheBalanceUnit() {
        assertEquals(500_000L, assertNotNull(BankParserRegistry.parse("blubank",
            "بلوبانک برداشت مبلغ: 500,000 ریال موجودی 90,000 تومان")).amount)
    }

    @Test
    fun colloquialTomanAndPersianDigitsAreAccepted() {
        assertEquals(500_000L, assertNotNull(BankParserRegistry.parse("989100001302",
            "برداشت ۵۰٬۰۰۰ تومن موجودی ۹۰۰٬۰۰۰ ریال")).amount)
    }

    @Test
    fun unitlessAmountKeepsTheExistingRialContract() {
        assertEquals(500_000L, assertNotNull(BankParserRegistry.parse("989100001303",
            "برداشت 500,000 موجودی 900,000")).amount)
    }

    @Test
    fun overflowingTomanDoesNotTurnTheBalanceIntoATransaction() {
        assertNull(BankParserRegistry.parse("989100001304",
            "برداشت 922337203685477581 تومان موجودی 900,000 ریال"))
    }

    @Test
    fun balanceOnlyAmountIsNotATransaction() {
        assertNull(BankParserRegistry.parse("989100001305", "برداشت انجام شد موجودی 900,000 ریال"))
    }
}
