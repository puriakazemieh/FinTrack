package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Transaction

class DeleteTransaction(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
//        if (transaction.type == TransactionType.INCOME) {
//            repository.decreaseBalanceFinancialSource(
//                transaction.financialSourceId,
//                transaction.amount
//            )
//        } else {
//            repository.increaseBalanceFinancialSource(
//                transaction.financialSourceId,
//                transaction.amount
//            )
//        }
        repository.decreaseBalanceFinancialSource(transaction.financialSourceId, transaction.amount)
        repository.deleteTransaction(transaction)
    }
}
