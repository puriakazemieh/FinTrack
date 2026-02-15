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
        val impact = transaction.balanceImpact()
        return repository.addTransactionWithBalance(transaction, tagIds, personIds, impact)
    }
}
