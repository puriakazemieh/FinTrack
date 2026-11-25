package com.kazemieh.domain.notification

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.SyncStatus

/** Epoch milliseconds; null suppresses reminders for inactive or unavailable records. */
fun fixedExpenseReminderTime(expense: FixedExpense): Long? {
    if (expense.id <= 0 || !expense.isActive || !expense.reminderEnabled || expense.syncStatus == SyncStatus.DELETED) return null
    return expense.reminderTime ?: expense.nextDueDate
}
