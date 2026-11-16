package com.kazemieh.common.sms

import com.kazemieh.common.toEnglishDigits

/** One integer/rial contract for incoming messages and review of older persisted drafts. */
object SmsAmountParser {
    private val explicitAmount = Regex("""([\d,]+)\s*(ریال|تومان|تومن)""")
    private val groupedAmount = Regex("""\d{1,3}(?:,\d{3})+""")

    fun amountInRials(raw: String, unit: String?): Long? {
        val value = normalize(raw).replace(",", "").toLongOrNull()?.takeIf { it > 0 } ?: return null
        return if (unit == "تومان" || unit == "تومن") {
            value.takeIf { it <= Long.MAX_VALUE / 10 }?.times(10)
        } else value
    }

    fun hasExplicitAmount(body: String): Boolean = explicitAmount.containsMatchIn(normalize(body))

    fun transactionAmountInRials(body: String, allowUnitless: Boolean = true): Long? {
        val text = normalize(body)
        val balancePosition = listOf("موجودی", "مانده").mapNotNull {
            text.indexOf(it).takeIf { index -> index >= 0 }
        }.minOrNull()
        val matches = explicitAmount.findAll(text).toList()
        if (matches.isNotEmpty()) {
            val match = matches.firstOrNull { balancePosition == null || it.range.first < balancePosition }
                ?: return null
            return amountInRials(match.groupValues[1], match.groupValues[2])
        }
        if (!allowUnitless) return null
        val match = groupedAmount.findAll(text).firstOrNull {
            balancePosition == null || it.range.first < balancePosition
        } ?: return null
        return amountInRials(match.value, null)
    }

    private fun normalize(value: String): String = value.toEnglishDigits()
        .replace('ي', 'ی').replace('ك', 'ک').replace('٬', ',').replace('،', ',')
}
