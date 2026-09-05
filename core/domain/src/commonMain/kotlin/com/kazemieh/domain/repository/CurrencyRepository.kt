package com.kazemieh.domain.repository

interface CurrencyRepository {
    suspend fun batchConvertCurrency(rate: Double, oldCurrencyCode: String, newCurrencyCode: String)
}
