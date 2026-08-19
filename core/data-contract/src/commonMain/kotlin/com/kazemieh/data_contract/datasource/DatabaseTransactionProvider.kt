package com.kazemieh.data_contract.datasource

interface DatabaseTransactionProvider {
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}
