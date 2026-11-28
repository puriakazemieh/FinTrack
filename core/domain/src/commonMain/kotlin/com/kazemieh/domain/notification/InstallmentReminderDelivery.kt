package com.kazemieh.domain.notification

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.SyncStatus

/** Resolve stored jobs from current state; a legacy hash must identify exactly one record. */
fun resolveInstallmentReminder(
    installments: List<Installment>,
    recordId: Long?,
    notificationId: Int,
    scheduledTimeMillis: Long?,
    nowMillis: Long
): Installment? {
    if (recordId != null && recordId <= 0) return null
    val records = installments.filter { it.id > 0 && it.syncStatus != SyncStatus.DELETED }
    val installment = if (recordId != null) records.singleOrNull { it.id == recordId }
        else records.singleOrNull { "installment_${it.id}".hashCode() == notificationId }
    installment ?: return null
    if (!installment.reminderEnabled || installment.isCompleted || installment.totalInstallments <= 0 ||
        installment.paidInstallments >= installment.totalInstallments) return null
    val due = installment.nextDueDate
    if (due > nowMillis || (scheduledTimeMillis != null && due != scheduledTimeMillis)) return null
    return installment
}
