package com.kazemieh.sms_reader

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toEnglishDigits
import kotlin.time.Clock

abstract class BaseBankParser : BankParser {
    protected fun extractAmount(body: String, regex: Regex): Int {
        val match = regex.find(body)
        return match?.groupValues?.get(1)
            ?.replace(",", "")
            ?.replace("،", "")
            ?.toEnglishDigits()
            ?.toIntOrNull() ?: 0
    }

    protected fun extractSourceIdentifier(body: String): String? {
        // Look for 4 digits after keywords like "کارت", "حساب", "به", "از"
        val regex = """(?:کارت|حساب|به|از)[:\s]*[\d\*]*(\d{4})""".toRegex()
        return regex.find(body)?.groupValues?.get(1)
    }

    protected fun createDraft(
        sender: String,
        body: String,
        amount: Int,
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

    override fun parse(sender: String, body: String): SmsDraft? {
        // Filter out balance-only messages
        if (body.contains("مانده") && !body.contains("واریز") && !body.contains("برداشت") && !body.contains("خرید") && !body.contains("پرداخت")) {
            return null
        }

        // Common Persian patterns for bank SMS
        val amountRegex = """(?:مبلغ|واريز|برداشت)[:\s]*([\d,،۰-۹]+)""".toRegex()
        val amount = extractAmount(body, amountRegex)
        if (amount <= 0) return null

        val type = when {
            body.contains("واریز") -> TransactionType.INCOME
            body.contains("خرید") || body.contains("برداشت") || body.contains("پرداخت") -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE // Default
        }

        val sourceIdentifier = extractSourceIdentifier(body)
        
        // Extracting bank name from sender name if possible
        val bankDisplayName = sender.replace("Bank", "").replace("bank", "").trim()

        return createDraft(sender, body, amount, type, bankDisplayName, sourceIdentifier, confidence = 80)
    }
}

// Keeping specific parsers for better accuracy on major banks
class BluParser : BaseBankParser() {
    override val bankName: String = "Blu"
    override val senderNumbers: List<String> = listOf("blubank", "V.Bank", "Blubank")

    override fun parse(sender: String, body: String): SmsDraft? {
        if (!body.contains("بلوبانک")) return null
        val amount = extractAmount(body, """مبلغ:\s*([\d,،۰-۹]+)""".toRegex())
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
        val amount = extractAmount(body, """مبلغ\s*([\d,،۰-۹]+)""".toRegex())
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
        val amount = extractAmount(body, """مبلغ\s*([\d,،۰-۹]+)""".toRegex())
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
        val amount = extractAmount(body, """مبلغ:\s*([\d,،۰-۹]+)""".toRegex())
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
        val amount = extractAmount(body, """مبلغ:\s*([\d,،۰-۹]+)""".toRegex())
        val type = if (body.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        val sourceIdentifier = extractSourceIdentifier(body)
        return createDraft(sender, body, amount, type, sourceIdentifier = sourceIdentifier)
    }
}
