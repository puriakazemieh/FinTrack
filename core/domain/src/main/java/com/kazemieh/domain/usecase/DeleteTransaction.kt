package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Transaction

class DeleteTransaction(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        if (transaction.financialSourceEndId != null) {
            repository.increaseBalanceFinancialSource(
                transaction.financialSourceId,
                transaction.amount + (transaction.amountTransfer)
            )
            repository.increaseBalanceFinancialSource(
                transaction.financialSourceEndId!!,
                (transaction.amount).times(-1)
            )
        } else {
            repository.increaseBalanceFinancialSource(
                transaction.financialSourceId,
                transaction.amount.times(-1)
            )
        }

        repository.deleteTransaction(transaction)
    }
}
