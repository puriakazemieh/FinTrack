package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class FixedExpense(
    val id: Long = 0,
    val amount: Long,
    val categoryId: Long,
    val categoryName: String? = null,
    val sourceId: Long,
    val sourceName: String? = null,
    val description: String? = null,
    val recurrence: RecurrenceType,
    val startDate: Long,
    val nextDueDate: Long,
    // End of the custom date range (RecurrenceType.CUSTOM). Null for the other recurrences.
    val endDate: Long? = null,
    val isAutoPostEnabled: Boolean = false,
    val isActive: Boolean = true,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

enum class RecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM,
    ONCE
}
