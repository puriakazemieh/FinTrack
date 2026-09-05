package com.kazemieh.data_contract.datasource

interface CurrencyLocalDataSource {
    suspend fun batchConvertCurrency(rate: Double, oldCurrencyCode: String, newCurrencyCode: String)
}
