package com.kazemieh.data.repository

import com.kazemieh.data_contract.datasource.CurrencyLocalDataSource
import com.kazemieh.domain.repository.CurrencyRepository

class CurrencyRepositoryImpl(
    private val localDataSource: CurrencyLocalDataSource
) : CurrencyRepository {
    override suspend fun batchConvertCurrency(rate: Double, oldCurrencyCode: String, newCurrencyCode: String) {
        localDataSource.batchConvertCurrency(rate, oldCurrencyCode, newCurrencyCode)
    }
}
