package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Check(
    val id: Long = 0,
    val amount: Long,
    val date: Long,
    val dueDate: Long,
    val status: CheckStatus = CheckStatus.PENDING,
    val personId: Long,
    val categoryId: Long? = null,
    val sourceId: Long? = null,
    val tagIds: List<Long>? = null,
    val reminderEnabled: Boolean = true,
    val personName: String? = null,
    val photoPath: String? = null,
    val description: String? = null,
    val isIncoming: Boolean = false, // True if received from others, False if issued by me
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

enum class CheckStatus {
    PENDING,
    PASSED,
    REJECTED,
    CANCELLED
}
