package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
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
        if (newTransaction.type == TransactionType.TRANSFER && newTransaction.sourceId == newTransaction.sourceEndId) {
            throw IllegalArgumentException("Cannot transfer to the same account")
        }

        val oldImpact = oldTransaction.balanceImpact()
        val newImpact = newTransaction.balanceImpact()

        val allSourceIds = (oldImpact.keys + newImpact.keys).toSet()
        val deltas = buildMap {
            allSourceIds.forEach { sourceId ->
                val delta = (newImpact[sourceId] ?: 0L) - (oldImpact[sourceId] ?: 0L)
                if (delta != 0L) put(sourceId, delta)
            }
        }

        return repository.updateTransactionWithBalance(newTransaction, tagIds, personIds, deltas)
    }


}

