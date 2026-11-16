package com.kazemieh.common.model

/** Bank identity shared by financial matching and presentation; contains no UI resources. */
enum class BankIdentity(vararg val aliases: String) {
    BluBank("blu", "blubank", "بلو", "بلوبانک", "بلو بانک"),
    Mellat("mellat", "ملت"), Saderat("saderat", "صادرات"),
    Pasargad("pasargad", "پاسارگاد"), Saman("saman", "سامان"),
    Melli("melli", "ملی"), Tejarat("tejarat", "تجارت"), Sepah("sepah", "سپه"),
    Resalat("resalat", "رسالت"), Parsian("parsian", "پارسیان"),
    Ayandeh("ayandeh", "آینده"), Keshavarzi("keshavarzi", "کشاورزی"),
    Shahr("shahr", "شهر"), Maskan("maskan", "مسکن"), Refah("refah", "رفاه");

    fun matchesSourceName(name: String): Boolean {
        val text = " ${normalizeBankName(name)} "
        return aliases.any { text.contains(" ${normalizeBankName(it)} ") }
    }

    companion object {
        fun fromName(name: String): BankIdentity? {
            val normalized = normalizeBankName(name).removePrefix("بانک").trim().removePrefix("bank ").trim()
            return entries.firstOrNull { bank -> bank.aliases.any { normalizeBankName(it) == normalized } }
        }
    }
}

private fun normalizeBankName(value: String): String = value.lowercase()
    .replace('ي', 'ی').replace('ك', 'ک')
    .map { if (it.isLetterOrDigit()) it else ' ' }.joinToString("")
    .trim().replace(Regex("\\s+"), " ")
