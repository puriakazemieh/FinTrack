package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.repository.BudgetRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.util.balanceImpact

class AddTransactionUseCase(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val notificationManager: NotificationManager
) {
    suspend operator fun invoke(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        val impact = transaction.balanceImpact()
        val resultId = repository.addTransactionWithBalance(transaction, tagIds, personIds, impact)

        // Check budget threshold for expenses
        if (transaction.type == TransactionType.EXPENSE) {
            checkBudgetThreshold(transaction.categoryId)
        }

        return resultId
    }

    private suspend fun checkBudgetThreshold(categoryId: Long) {
        val budgetProgress = budgetRepository.getBudgetWithProgressByCategory(categoryId) ?: return

        if (budgetProgress.budget.isAlertEnabled && budgetProgress.progress >= 0.8f) {
            notificationManager.showBudgetAlert(
                categoryId = categoryId.toInt(),
                categoryName = budgetProgress.category?.name ?: "",
                progressPercentage = (budgetProgress.progress * 100).toInt()
            )
        }
    }
}
