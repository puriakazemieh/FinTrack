package com.kazemieh.domain.usecase

data class TransactionUseCaseGroup(
    val addTransactionUseCase: AddTransactionUseCase,
    val getDefaultCategoryUseCase: GetDefaultCategoryUseCase,
    val getTransferCategoryUseCase: GetTransferCategoryUseCase,
    val getDefaultFinancialSourceUseCase: GetDefaultFinancialSourceUseCase,
    val deleteTransactionUseCase: DeleteTransactionUseCase,
    val updateTransactionUseCase: UpdateTransactionUseCase,
    val observeTransactionsUseCase: ObserveTransactionsUseCase,
    val observeCategorySumsUseCase: ObserveCategorySumsUseCase,
    val observeSourcesUseCase: ObserveSourcesUseCase
)
