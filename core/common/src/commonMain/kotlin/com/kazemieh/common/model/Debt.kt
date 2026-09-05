package com.kazemieh.common.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Debt(
    val id: Long = 0,
    val personId: Long,
    val amount: Long,
    val categoryId: Long? = null,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val dueDate: Long? = null,
    val sourceId: Long? = null,
    val description: String? = null,
    val type: DebtType,
    val isSettled: Boolean = false,
    val reminderEnabled: Boolean = false,
    val personName: String? = null,
    val sourceName: String? = null,
    val currencyCode: String = "IRT",
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

@Serializable
data class DebtWithRelations(
    val debt: Debt,
    val person: Person,
    val category: Category? = null,
    val source: Source? = null,
    val tags: List<Tag> = emptyList()
)

@Serializable
enum class DebtType(val value: Int) {
    OWED_TO_ME(1), // طلب (بستانکار)
    OWED_BY_ME(2); // بدهی (بدهکار)

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
