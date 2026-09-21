package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.analytics.ProductEvent
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.repository.BudgetRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.util.balanceImpact

class AddTransactionUseCase(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val notificationManager: NotificationManager,
    private val updateStreak: UpdateStreakUseCase,
    private val checkAchievements: CheckAchievementsUseCase,
    private val updateXP: UpdateXPUseCase,
    private val analytics: AnalyticsService = NoOpAnalyticsService()
) {
    suspend operator fun invoke(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        if (transaction.type == TransactionType.TRANSFER && transaction.sourceId == transaction.sourceEndId) {
            throw IllegalArgumentException("Cannot transfer to the same account")
        }

        val impact = transaction.balanceImpact()
        val resultId = repository.addTransactionWithBalance(transaction, tagIds, personIds, impact)

        // This is deliberately checked after the insert and lives in the common creation path,
        // so SMS, asset, cheque and scheduled transaction flows cannot emit it prematurely.
        if (resultId > 0L && repository.getTransactionCount() == 1L) {
            analytics.track(ProductEvent.FirstTransactionCompleted)
        }

        // Check budget threshold for expenses
        if (transaction.type == TransactionType.EXPENSE) {
            checkBudgetThreshold(transaction.categoryId)
        }

        // Gamification
        updateStreak()
        updateXP(10) // Basic XP for adding transaction
        checkAchievements()

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
