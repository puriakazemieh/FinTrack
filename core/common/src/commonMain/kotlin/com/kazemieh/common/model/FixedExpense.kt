package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class FixedExpense(
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val categoryId: Long,
    val categoryName: String? = null,
    val sourceId: Long,
    val sourceName: String? = null,
    val description: String? = null,
    val recurrence: RecurrenceType,
    val startDate: Long,
    val nextDueDate: Long,
    // Optional end bound for the recurrence. Null means open-ended (no end date).
    val endDate: Long? = null,
    val isAutoPostEnabled: Boolean = false,
    val isActive: Boolean = true,
    val tagIds: List<Long> = emptyList(),
    val tagNames: List<String> = emptyList(),
    val personIds: List<Long> = emptyList(),
    val personNames: List<String> = emptyList(),
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

enum class RecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM,
    ONCE,
    NONE
}
