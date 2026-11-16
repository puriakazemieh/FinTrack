package com.kazemieh.sms_reader

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toEnglishDigits
import com.kazemieh.common.sms.SmsAmountParser
import kotlin.time.Clock

abstract class BaseBankParser : BankParser {
    protected fun extractAmount(body: String, regex: Regex): Long {
        val match = regex.find(body) ?: return 0L
        val amountGroup = match.groups[1] ?: return 0L
        val unit = Regex("""^\s*(ریال|تومان|تومن)""")
            .find(body.substring(amountGroup.range.last + 1))?.groupValues?.get(1)
        return SmsAmountParser.amountInRials(amountGroup.value, unit) ?: 0L
    }

    protected fun extractSourceIdentifier(body: String): String? {
        // Look for 4 digits after keywords like "کارت", "حساب", "به", "از"
        val regex = """(?:کارت|حساب|به|از)[:\s]*[\d\*]*(\d{4})""".toRegex()
        return regex.find(body.toEnglishDigits())?.groupValues?.get(1)
    }

    protected fun createDraft(
        sender: String,
        body: String,
        amount: Long,
        type: TransactionType,
        bankName: String? = null,
        sourceIdentifier: String? = null,
        confidence: Int = 100
    ): SmsDraft {
        return SmsDraft(
            sender = sender,
            body = body,
            amount = amount,
            bankName = bankName ?: this.bankName,
            type = type,
            sourceIdentifier = sourceIdentifier,
            timeStamp = Clock.System.now().toEpochMilliseconds(),
            date = "",
            confidence = confidence
        )
    }
}

class GenericBankParser : BaseBankParser() {
    override val bankName: String = "Generic"
    override val senderNumbers: List<String> = emptyList() // Not used for generic

    // Direction keywords. Some banks (e.g. Blu) phrase deposits as "به حساب شما نشست" and never say
    // "مبلغ", so we key off the verb, not a fixed "مبلغ: <n>" layout.
    private val incomeKeywords = listOf("واریز", "نشست", "بستانکار", "دریافت", "وصول")
    private val expenseKeywords = listOf("برداشت", "خرید", "پرداخت", "کسر", "خرج", "حواله", "بدهکار")

    // Friendly names so the notification/sheet shows the bank instead of a raw shortcode.
    private val bankNameHints = mapOf(
        "بلو" to "بلو", "بلوبانک" to "بلو", "ملت" to "ملت", "صادرات" to "صادرات",
        "پاسارگاد" to "پاسارگاد", "سامان" to "سامان", "ملی" to "ملی", "تجارت" to "تجارت",
        "سپه" to "سپه", "رسالت" to "رسالت", "پارسیان" to "پارسیان", "آینده" to "آینده",
        "کشاورزی" to "کشاورزی", "شهر" to "شهر", "مسکن" to "مسکن", "رفاه" to "رفاه"
    )

    override fun parse(sender: String, body: String): SmsDraft? {
        // Normalize Arabic yeh/kaf, Persian digits, and the Persian/Arabic thousands separators
        // (٬ U+066C, ، U+060C) to an ASCII comma so keyword/amount matching is reliable regardless
        // of which keyboard the bank used. Without this, "۵۰۰٬۰۰۰ ریال" fails to parse.
        val text = body.toEnglishDigits()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('٬', ',')
            .replace('،', ',')

        val isIncome = incomeKeywords.any { text.contains(it) }
        val isExpense = expenseKeywords.any { text.contains(it) }
        // Must look like an actual transaction, not a balance/OTP/promo message.
        if (!isIncome && !isExpense) return null

        val txAmount = SmsAmountParser.transactionAmountInRials(text) ?: return null

        val type = when {
            isIncome && !isExpense -> TransactionType.INCOME
            isExpense && !isIncome -> TransactionType.EXPENSE
            isIncome -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        val sourceIdentifier = extractSourceIdentifier(text)
        val bankDisplayName = bankNameHints.entries.firstOrNull { text.contains(it.key) }?.value
            ?: sender.replace("Bank", "").replace("bank", "").trim()
        val amountLong = txAmount

        return createDraft(sender, body, amountLong, type, bankDisplayName, sourceIdentifier, confidence = 75)
    }
}

// Keeping specific parsers for better accuracy on major banks
class BluParser : BaseBankParser() {
    override val bankName: String = "Blu"
    override val senderNumbers: List<String> = listOf("blubank", "V.Bank", "Blubank")

    override fun parse(sender: String, body: String): SmsDraft? {
        if (!body.contains("بلوبانک")) return null
        val amount = extractAmount(body, """مبلغ:\s*([\d,،٬۰-۹]+)""".toRegex())
        val type = if (body.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        val sourceIdentifier = extractSourceIdentifier(body)
        return createDraft(sender, body, amount, type, sourceIdentifier = sourceIdentifier)
    }
}

class MellatParser : BaseBankParser() {
    override val bankName: String = "Mellat"
    override val senderNumbers: List<String> = listOf("BankMellat", "MellatBank", "بانک ملت")

    override fun parse(sender: String, body: String): SmsDraft? {
        if (!body.contains("ملت")) return null
        val amount = extractAmount(body, """مبلغ\s*([\d,،٬۰-۹]+)""".toRegex())
        val type = if (body.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        val sourceIdentifier = extractSourceIdentifier(body)
        return createDraft(sender, body, amount, type, sourceIdentifier = sourceIdentifier)
    }
}

class SaderatParser : BaseBankParser() {
    override val bankName: String = "Saderat"
    override val senderNumbers: List<String> = listOf("SaderatBank", "BankSaderat", "BS")

    override fun parse(sender: String, body: String): SmsDraft? {
        if (!body.contains("صادرات")) return null
        val amount = extractAmount(body, """مبلغ\s*([\d,،٬۰-۹]+)""".toRegex())
        val type = if (body.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        val sourceIdentifier = extractSourceIdentifier(body)
        return createDraft(sender, body, amount, type, sourceIdentifier = sourceIdentifier)
    }
}

class PasargadParser : BaseBankParser() {
    override val bankName: String = "Pasargad"
    override val senderNumbers: List<String> = listOf("Pasargad", "BPI")

    override fun parse(sender: String, body: String): SmsDraft? {
        if (!body.contains("پاسارگاد")) return null
        val amount = extractAmount(body, """مبلغ:\s*([\d,،٬۰-۹]+)""".toRegex())
        val type = if (body.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        val sourceIdentifier = extractSourceIdentifier(body)
        return createDraft(sender, body, amount, type, sourceIdentifier = sourceIdentifier)
    }
}

class SamanParser : BaseBankParser() {
    override val bankName: String = "Saman"
    override val senderNumbers: List<String> = listOf("SamanBank", "Saman")

    override fun parse(sender: String, body: String): SmsDraft? {
        if (!body.contains("سامان")) return null
        val amount = extractAmount(body, """مبلغ:\s*([\d,،٬۰-۹]+)""".toRegex())
        val type = if (body.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        val sourceIdentifier = extractSourceIdentifier(body)
        return createDraft(sender, body, amount, type, sourceIdentifier = sourceIdentifier)
    }
}
