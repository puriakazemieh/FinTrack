package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long = 0,
    val iconId: Int = 1,
    val colorId: Int = 1,
    val startDate: Long = 0,
    val endDate: Long? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val monthlyTarget: Long = 0,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity {
    val progress: Float
        get() = if (targetAmount > 0) savedAmount.toFloat() / targetAmount.toFloat() else 0f
    
    val percent: Int
        get() = (progress * 100).toInt()
}
