package com.kazemieh.sms_reader

import com.kazemieh.common.model.TransactionType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BankParserRegistryTest {
    @BeforeTest
    fun initializeParsers() {
        BankParserRegistry.init(listOf(BluParser()), GenericBankParser())
    }

    @Test
    fun purchaseOtpIsNotATransaction() {
        assertNull(BankParserRegistry.parse("989100001234", "رمز پویا 123456 برای خرید مبلغ 500,000 ریال بانک تست"))
        assertNull(BankParserRegistry.parse("blubank", "بلوبانک رمز دوم: 123456 مبلغ: 500,000 ریال"))
        assertNull(BankParserRegistry.parse("BANK", "OTP: 123456 Purchase 500,000 ریال"))
    }

    @Test
    fun actualPurchaseKeepsAmountAndDirection() {
        val draft = assertNotNull(
            BankParserRegistry.parse("989100001234", "بانک تست برداشت مبلغ 120,000 ریال موجودی 900,000 ریال")
        )
        assertEquals(120_000L, draft.amount)
        assertEquals(TransactionType.EXPENSE, draft.type)
    }

    @Test
    fun specificParserWithoutAnAmountDoesNotProduceADraft() {
        assertNull(BankParserRegistry.parse("blubank", "بلوبانک اطلاعیه خدمات"))
    }
}
