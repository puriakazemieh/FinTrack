package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: Long? = null,
    val categoryId: Long,
    val amount: Long,
    val period: BudgetPeriod,
    val startAt: Long,
    val isAlertEnabled: Boolean = true
)

@Serializable
enum class BudgetPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

data class BudgetWithProgress(
    val budget: Budget,
    val category: Category?,
    val spentAmount: Long,
    val progress: Float // 0.0 to 1.0+
)
