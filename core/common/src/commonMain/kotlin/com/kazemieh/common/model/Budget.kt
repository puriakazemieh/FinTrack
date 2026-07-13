package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: Long? = null,
    val categoryId: Long,
    val amount: Long,
    val period: BudgetPeriod,
    val startAt: Long,
    val tagIds: List<Long>? = null,
    val sourceId: Long? = null,
    val isAlertEnabled: Boolean = true,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

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
