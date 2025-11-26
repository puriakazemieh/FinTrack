package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Transaction

class AddTransaction(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        if (transaction.financialSourceEndId != null) {
            repository.increaseBalanceFinancialSource(
                transaction.financialSourceEndId!!,
                transaction.amount
            )
            repository.increaseBalanceFinancialSource(
                transaction.financialSourceId,
                (transaction.amount + (transaction.amountTransfer)).times(-1)
            )
        } else {
            repository.increaseBalanceFinancialSource(
                transaction.financialSourceId,
                transaction.amount
            )
        }
        return repository.insertTransaction(transaction, tagIds, personIds)
    }
}
