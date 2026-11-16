package com.kazemieh.designsystem.model

/** Accept existing persisted English/Persian bank names without migrating draft data. */
fun bankForSms(name: String): Bank {
    val normalized = name.lowercase().replace('ي', 'ی').replace('ك', 'ک')
        .replace(" ", "").replace("\u200c", "").removePrefix("بانک")
    return when (normalized) {
        "blu", "blubank", "بلو", "بلوبانک" -> Bank.BluBank
        "mellat", "ملت" -> Bank.Mellat
        "saderat", "صادرات" -> Bank.Saderat
        "pasargad", "پاسارگاد" -> Bank.Pasargad
        "saman", "سامان" -> Bank.Saman
        "melli", "ملی" -> Bank.Melli
        "tejarat", "تجارت" -> Bank.Tejarat
        "sepah", "سپه" -> Bank.Sepah
        "resalat", "رسالت" -> Bank.Resalat
        "parsian", "پارسیان" -> Bank.Parsian
        "ayandeh", "آینده" -> Bank.Ayandeh
        "keshavarzi", "کشاورزی" -> Bank.Keshavarzi
        "shahr", "شهر" -> Bank.Shahr
        "maskan", "مسکن" -> Bank.Maskan
        "refah", "رفاه" -> Bank.Refah
        else -> Bank.Unknown
    }
}
