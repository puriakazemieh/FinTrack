package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.Transaction
import com.kazemieh.domain.util.balanceImpact

class DeleteTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        val impact = transaction.balanceImpact().mapValues { -it.value }
        repository.deleteTransactionWithBalance(transaction, impact)
    }
}
