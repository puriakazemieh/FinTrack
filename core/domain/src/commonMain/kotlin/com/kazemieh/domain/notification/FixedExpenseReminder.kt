package com.kazemieh.domain.notification

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.SyncStatus

/** Epoch milliseconds; null suppresses reminders for inactive or unavailable records. */
fun fixedExpenseReminderTime(expense: FixedExpense): Long? {
    if (expense.id <= 0 || !expense.isActive || !expense.reminderEnabled || expense.syncStatus == SyncStatus.DELETED) return null
    return expense.reminderTime ?: expense.nextDueDate
}

/** Old jobs resolve only a unique record; a queued payload cannot override current settings. */
fun resolveFixedExpenseReminder(
    expenses: List<FixedExpense>,
    recordId: Long?,
    notificationId: Int,
    scheduledTimeMillis: Long?,
    nowMillis: Long
): FixedExpense? {
    if (recordId != null && recordId <= 0) return null
    val records = expenses.filter { it.id > 0 && it.syncStatus != SyncStatus.DELETED }
    val expense = if (recordId != null) records.singleOrNull { it.id == recordId }
        else records.singleOrNull { "fixed_expense_${it.id}".hashCode() == notificationId }
    val due = expense?.let(::fixedExpenseReminderTime) ?: return null
    if (due > nowMillis || (scheduledTimeMillis != null && due != scheduledTimeMillis)) return null
    return expense
}
