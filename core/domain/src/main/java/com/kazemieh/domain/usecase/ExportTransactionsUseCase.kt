package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class ExportTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): String {
        val allTransactions = repository.getAllTransactions().first()
        return Json.encodeToString(allTransactions)
    }
}
