package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.Transaction

class AddTransaction(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        /* if (transaction.type == TransactionType.INCOME) {
             repository.increaseBalanceFinancialSource(transaction.financialSourceId, transaction.amount)
         } else {
             repository.decreaseBalanceFinancialSource(transaction.financialSourceId, transaction.amount)
         }*/
        repository.increaseBalanceFinancialSource(transaction.financialSourceId, transaction.amount)
        return repository.insertTransaction(transaction, tagIds, personIds)
    }
}
