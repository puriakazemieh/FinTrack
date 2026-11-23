package com.kazemieh.financialsource.ui.list

import com.kazemieh.common.MoneyPrivacy
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.withLRM
import com.kazemieh.common.model.Source
import com.kazemieh.money.Currency
import com.kazemieh.money.CurrencyProvider
import kotlinx.serialization.json.Json

/** A null balance means the total cannot be represented safely as a Long. */
data class SourceCurrencyBalance(val currency: Currency, val balance: Long?) {
    fun formattedBalance(maskAmounts: Boolean = MoneyPrivacy.maskAmounts): String? {
        if (maskAmounts) return "••••••"
        val amount = balance ?: return null
        val digits = amount.toString().removePrefix("-").toPersianPrice()
        return when {
            amount < 0 -> "- $digits".withLRM()
            amount > 0 -> "+ $digits".withLRM()
            else -> digits
        }
    }
}

internal fun sourceCurrency(value: String): Currency =
    runCatching { Json.decodeFromString<Currency>(value) }.getOrNull()?.takeIf { it.code.isNotBlank() }
        ?: listOf(Currency.TOMAN, Currency.RIAL).firstOrNull { it.displayName == value }
        ?: CurrencyProvider.allCurrencies.firstOrNull { it.code == value || it.displayName == value }
        ?: if (value.isBlank()) Currency.TOMAN else Currency(value, value, value)

internal fun sourceCurrencyBalances(sources: List<Source>): List<SourceCurrencyBalance> =
    sources.groupBy { sourceCurrency(it.currencyCode).code }.entries.sortedBy { it.key }.map { (_, group) ->
        var total: Long? = 0L
        val positive = group.filter { it.balance >= 0 }.iterator()
        val negative = group.filter { it.balance < 0 }.iterator()
        // Consume an opposite sign first, so a representable final sum cannot overflow mid-way.
        while (total != null && (positive.hasNext() || negative.hasNext())) {
            val current = total
            val amount = when {
                current >= 0 && negative.hasNext() -> negative.next().balance
                positive.hasNext() -> positive.next().balance
                else -> negative.next().balance
            }
            val next = current + amount
            total = if (((current xor next) and (amount xor next)) < 0) null else next
        }
        SourceCurrencyBalance(sourceCurrency(group.first().currencyCode), total)
    }
