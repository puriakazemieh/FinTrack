package com.kazemieh.common.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock

@Serializable
data class Installment(
    val id: Long,
    val title: String,
    val totalAmount: Long, // مبلغ کل
    val installmentAmount: Long, // مبلغ هر قسط
    val totalInstallments: Int, // تعداد کل اقساط
    val paidInstallments: Int, // تعداد اقساط پرداخت شده
    val categoryId: Long,
    val sourceId: Long,
    val startDate: Long, // تاریخ شروع
    val nextDueDate: Long, // تاریخ سررسید بعدی
    val frequency: InstallmentFrequency,
    val description: String? = null,
    val isCompleted: Boolean = false, // آیا تمام اقساط پرداخت شده؟
    val reminderEnabled: Boolean = true,
    val postAsTransaction: Boolean = true,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

@Serializable
data class InstallmentWithRelations(
    val installment: Installment,
    val category: Category?,
    val source: Source?,
    val tags: List<Tag> = emptyList(),
    val persons: List<Person> = emptyList()
)

@Serializable
enum class InstallmentFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class InstallmentStatus {
    UPCOMING,
    PAID,
    OVERDUE
}
