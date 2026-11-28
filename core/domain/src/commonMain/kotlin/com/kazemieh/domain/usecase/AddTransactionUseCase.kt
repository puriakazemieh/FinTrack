package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.FixedExpensePostingRequest
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.analytics.ProductEvent
import com.kazemieh.common.analytics.CrashReporter
import com.kazemieh.common.analytics.NoOpCrashReporter
import kotlinx.coroutines.CancellationException
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
    private val analytics: AnalyticsService = NoOpAnalyticsService(),
    private val crashReporter: CrashReporter = NoOpCrashReporter()
) {
    suspend operator fun invoke(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        smsDraftId: Long? = null,
        fixedExpensePosting: FixedExpensePostingRequest? = null,
    ): Long {
        require(smsDraftId == null || fixedExpensePosting == null) { "A transaction can claim only one event" }
        if (fixedExpensePosting != null) {
            require(tagIds.toSet() == fixedExpensePosting.expense.tagIds.toSet() &&
                personIds.toSet() == fixedExpensePosting.expense.personIds.toSet()) { "Fixed expense relations changed" }
        }
        if (transaction.type == TransactionType.TRANSFER && transaction.sourceId == transaction.sourceEndId) {
            throw IllegalArgumentException("Cannot transfer to the same account")
        }

        val impact = transaction.balanceImpact()
        val resultId = if (fixedExpensePosting != null) {
            repository.addFixedExpenseTransactionWithBalance(fixedExpensePosting, transaction)
        } else if (smsDraftId != null) {
            repository.addSmsTransactionWithBalance(smsDraftId, transaction, tagIds, personIds, impact)
        } else repository.addTransactionWithBalance(transaction, tagIds, personIds, impact)
        if (resultId <= 0L) return resultId

        if (smsDraftId != null) postCommitEffect("sms_notification_cleanup_failed") {
            notificationManager.cancelNotification(smsDraftId.toInt(), NotificationManager.CHANNEL_SMS, smsDraftId)
        }

        // This is deliberately checked after the insert and lives in the common creation path,
        // so SMS, asset, cheque and scheduled transaction flows cannot emit it prematurely.
        postCommitEffect("transaction_first_event_failed") {
            if (repository.getTransactionCount() == 1L) analytics.track(ProductEvent.FirstTransactionCompleted)
        }

        // Check budget threshold for expenses
        if (transaction.type == TransactionType.EXPENSE) {
            postCommitEffect("transaction_budget_check_failed") { checkBudgetThreshold(transaction.categoryId) }
        }

        // Gamification
        postCommitEffect("transaction_streak_failed") { updateStreak() }
        postCommitEffect("transaction_xp_failed") { updateXP(10) }
        postCommitEffect("transaction_achievements_failed") { checkAchievements() }

        return resultId
    }

    private suspend fun postCommitEffect(operation: String, action: suspend () -> Unit) {
        try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Reporting must not turn an already committed financial write into a failed write.
            runCatching { crashReporter.recordException(e, operation) }
        }
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
