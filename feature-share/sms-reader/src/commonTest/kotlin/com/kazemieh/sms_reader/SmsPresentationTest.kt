package com.kazemieh.sms_reader

import com.kazemieh.designsystem.model.Bank
import com.kazemieh.money.Currency
import kotlin.test.Test
import kotlin.test.assertEquals

class SmsPresentationTest {
    @Test
    fun oldAndNewBankNamesUseTheSameDesignSystemBank() {
        for (name in listOf("Blu", "بلو", "بلوبانک", "بلو بانک")) {
            assertEquals(Bank.BluBank, smsBank(name))
        }
        assertEquals(Bank.Mellat, smsBank("Mellat"))
        assertEquals(Bank.Mellat, smsBank("بانک ملت"))
        assertEquals(Bank.Melli, smsBank("ملي"))
        assertEquals(Bank.Unknown, smsBank("989100001244"))
    }

    @Test
    fun preferenceSupportsBothCodeAndSerializedCurrency() {
        assertEquals(Currency.TOMAN, smsDisplayCurrency("IRT"))
        assertEquals(Currency.TOMAN, smsDisplayCurrency(""))
        assertEquals(Currency.TOMAN, smsDisplayCurrency("""{"code":"IRT","symbol":"تومان","displayName":"تومان"}"""))
        assertEquals(Currency.RIAL, smsDisplayCurrency("IRR"))
        assertEquals(Currency.RIAL, smsDisplayCurrency("USD"))
    }

    @Test
    fun formattedAmountKeepsItsExplicitUnit() {
        assertEquals("۵۰,۰۰۰ تومان", smsFormattedAmount(500_000L, Currency.TOMAN, "تومان"))
        assertEquals("۵۰۰,۰۰۰ Rial", smsFormattedAmount(500_000L, Currency.RIAL, "Rial"))
    }
}
