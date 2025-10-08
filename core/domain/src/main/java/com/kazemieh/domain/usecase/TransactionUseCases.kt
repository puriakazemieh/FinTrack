package com.kazemieh.domain.usecase

data class TransactionUseCases(
    val addTransaction: AddTransaction,
    val getDefaultCategoryUseCase: GetDefaultCategoryUseCase,
    val getDefaultFinancialSourceUseCase: GetDefaultFinancialSourceUseCase,
    val deleteTransaction: DeleteTransaction,
    val getAllTransactions: GetAllTransactions
)
