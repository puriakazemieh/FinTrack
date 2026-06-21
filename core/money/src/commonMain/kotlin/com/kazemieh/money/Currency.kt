package com.kazemieh.money

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

        fun valueOf(value: String): Currency {
            if (value.isEmpty()) return TOMAN
            return try {
                Json.decodeFromString<Currency>(value)
            } catch (e: Exception) {
                CurrencyProvider.allCurrencies.find {
                    it.code == value || it.displayName == value
                } ?: TOMAN
            }
        }
    }
}

@Serializable
enum class CurrencyType {
    FIAT,
    CRYPTO
}
