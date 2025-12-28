package com.kazemieh.domain.usecase

data class TransactionUseCases(
    val addTransaction: AddTransaction,
    val getDefaultCategoryUseCase: GetDefaultCategoryUseCase,
    val getTransferCategoryUseCase: GetTransferCategoryUseCase,
    val getDefaultFinancialSourceUseCase: GetDefaultFinancialSourceUseCase,
    val deleteTransaction: DeleteTransaction,
    val updateTransaction: UpdateTransaction,
    val getAllTransactionsFiltered: GetAllTransactionsFiltered,
    val getCategorySum: GetCategorySum
)
