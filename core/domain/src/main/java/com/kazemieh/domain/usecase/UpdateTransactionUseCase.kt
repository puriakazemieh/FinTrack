package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.util.balanceImpact

class UpdateTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        oldTransaction: Transaction,
        newTransaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {

        val oldImpact = oldTransaction.balanceImpact()
        val newImpact = newTransaction.balanceImpact()

        val allSourceIds = (oldImpact.keys + newImpact.keys).toSet()
        allSourceIds.forEach { sourceId ->
            val delta = (newImpact[sourceId] ?: 0) - (oldImpact[sourceId] ?: 0)
            if (delta != 0) repository.adjustSourceBalance(sourceId, delta)
        }
        return repository.updateTransaction(newTransaction, tagIds, personIds)
    }

}

