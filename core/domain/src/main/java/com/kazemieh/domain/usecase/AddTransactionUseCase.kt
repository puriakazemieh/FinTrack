package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.util.balanceImpact

class AddTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        val idTransaction = repository.addTransaction(transaction, tagIds, personIds)
        if (idTransaction >= 0L) {
            val impact = transaction.balanceImpact()
            impact.forEach { (sourceId, delta) ->
                repository.adjustSourceBalance(sourceId, delta)
            }
        }
        return idTransaction
    }
}
