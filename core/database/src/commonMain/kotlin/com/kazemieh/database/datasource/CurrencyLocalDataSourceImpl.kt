package com.kazemieh.database.datasource

import com.kazemieh.data_contract.datasource.CurrencyLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class CurrencyLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : CurrencyLocalDataSource {
    override suspend fun batchConvertCurrency(rate: Double, oldCurrencyCode: String, newCurrencyCode: String) = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        db.transaction {
            db.transactionQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
            db.sourceQueries.batchConvertBalances(rate, newCurrencyCode, now, oldCurrencyCode)
            db.budgetQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
            db.checkQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
            db.debtQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
            db.fixedExpenseQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
            db.goalQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
            db.installmentQueries.batchConvertAmounts(rate, newCurrencyCode, now, oldCurrencyCode)
        }
    }
}
