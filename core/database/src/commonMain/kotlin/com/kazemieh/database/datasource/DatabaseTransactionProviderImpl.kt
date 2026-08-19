package com.kazemieh.database.datasource

import com.kazemieh.data_contract.datasource.DatabaseTransactionProvider
import com.kazemieh.database.FinTrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseTransactionProviderImpl(
    private val db: FinTrackDatabase
) : DatabaseTransactionProvider {
    override suspend fun <R> runInTransaction(block: suspend () -> R): R = withContext(Dispatchers.Default) {
        db.transactionWithResult {
            block()
        }
    }
}
