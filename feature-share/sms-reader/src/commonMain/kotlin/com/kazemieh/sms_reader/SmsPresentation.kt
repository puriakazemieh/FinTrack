package com.kazemieh.sms_reader

import com.kazemieh.designsystem.model.Bank
import com.kazemieh.money.Currency
import com.kazemieh.money.MoneyFormatter

fun smsBank(name: String): Bank = com.kazemieh.designsystem.model.bankForSms(name)

// Foreign display currencies cannot relabel a bank's rial amount without an exchange rate.
fun smsDisplayCurrency(preference: String): Currency =
    if (Currency.valueOf(preference).code == "IRT") Currency.TOMAN else Currency.RIAL

fun smsDisplayAmount(amountInRials: Long, currency: Currency): Long =
    if (currency.code == "IRT") amountInRials / 10 else amountInRials

fun smsFormattedAmount(amountInRials: Long, currency: Currency, unitLabel: String): String =
    MoneyFormatter.format(smsDisplayAmount(amountInRials, currency), currency.copy(symbol = unitLabel))
