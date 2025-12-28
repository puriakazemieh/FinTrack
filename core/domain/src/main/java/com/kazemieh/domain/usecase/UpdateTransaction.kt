package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.TransactionRepository

class UpdateTransaction(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        oldTransaction: Transaction,
        newTransaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {

        val oldImpact = impact(oldTransaction)
        val newImpact = impact(newTransaction)

        val allSourceIds = (oldImpact.keys + newImpact.keys).toSet()

        for (sourceId in allSourceIds) {
            val delta = (newImpact[sourceId] ?: 0) - (oldImpact[sourceId] ?: 0)
            if (delta != 0) {
                repository.increaseBalanceFinancialSource(sourceId, delta)
            }
        }

        return repository.updateTransaction(newTransaction, tagIds, personIds)
    }

    private fun impact(tx: Transaction): Map<Long, Int> {
        return when (tx.type) {
            TransactionType.INCOME -> mapOf(tx.sourceId to tx.amount)
            TransactionType.EXPENSE -> mapOf(tx.sourceId to -tx.amount)
            TransactionType.TRANSFER -> {
                val fromDelta = -(tx.amount + tx.amountTransfer)
                val toDelta = tx.amount
                buildMap {
                    put(tx.sourceId, fromDelta)
                    tx.sourceEndId?.let { put(it, toDelta) }
                }
            }

            else -> mapOf()
        }
    }
}

