package com.kazemieh.domain.usecase

data class TransactionUseCases(
    val addTransaction: AddTransaction,
    val deleteTransaction: DeleteTransaction,
    val getAllTransactions: GetAllTransactions
)
