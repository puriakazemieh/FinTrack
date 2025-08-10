package com.kazemieh.domain.usecase

data class TransactionUseCases(
    val addTransaction: AddTransaction,
    val getAllTag: GetAllTag,
    val getAllFinancialSource: GetAllFinancialSource,
    val getAllCategory: GetAllCategory,
    val deleteTransaction: DeleteTransaction,
    val getAllTransactions: GetAllTransactions
)
