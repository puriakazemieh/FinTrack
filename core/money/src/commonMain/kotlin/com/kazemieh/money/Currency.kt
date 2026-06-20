package com.kazemieh.money

import kotlinx.serialization.Serializable

@Serializable
data class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val type: CurrencyType = CurrencyType.FIAT
) {
    companion object {
        val TOMAN = Currency("IRT", "تومان", "تومان", CurrencyType.FIAT)
        val RIAL = Currency("IRR", "ریال", "ریال", CurrencyType.FIAT)
    }
}

@Serializable
enum class CurrencyType {
    FIAT,
    CRYPTO
}
