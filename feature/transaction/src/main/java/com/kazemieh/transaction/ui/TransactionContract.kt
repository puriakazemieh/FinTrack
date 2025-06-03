package com.kazemieh.transaction.ui

import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations

sealed interface TransactionEvent {
    object LoadTransactions : TransactionEvent
    data class DeleteTransaction(val transaction: Transaction) : TransactionEvent
}

data class TransactionState(
    val transactions: List<TransactionWithRelations> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface TransactionEffect {
    data class ShowMessage(val message: Int) : TransactionEffect
}
