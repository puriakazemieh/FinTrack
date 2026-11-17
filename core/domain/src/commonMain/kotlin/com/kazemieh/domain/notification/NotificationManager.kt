package com.kazemieh.domain.notification

interface NotificationManager {
    fun createChannels()
    fun createChannel(id: String, name: String, importance: Int = 3)
    fun showNotification(id: Int, title: String, message: String, channelId: String, extraId: Long = -1L)
    fun hasPermission(): Boolean
    fun openSettings()
    fun showStickyNotification(id: Int, title: String, message: String, smsDraftId: Long = -1L)
    fun showBudgetAlert(categoryId: Int, categoryName: String, progressPercentage: Int)

    fun showInstallmentNotification(id: Int, title: String, message: String, installmentId: Long)

    /** Shows a dismissible one-tap "add transaction" notification, deep-linking into the add sheet. */
    fun showQuickAddNotification(title: String, message: String, actionLabel: String)
    fun cancelNotification(id: Int)
    /** Cancels only the owning type and record, even when another type uses the same integer ID. */
    fun cancelNotification(id: Int, channelId: String, recordId: Long? = null) = cancelNotification(id)

    companion object {
        const val CHANNEL_BUDGET = "budget_reminders"
        const val CHANNEL_INSTALLMENT = "installment_reminders_v4"
        const val CHANNEL_CHEQUE = "cheque_reminders"
        const val CHANNEL_SHOPPING = "shopping_reminders"
        const val CHANNEL_NOTE = "note_reminders"
        const val CHANNEL_SMS = "sms_detection"
        const val CHANNEL_DEBT = "debt_reminders"
        const val CHANNEL_QUICK_ADD = "quick_add"
        const val CHANNEL_FIXED_EXPENSE = "fixed_expense_reminders"
        const val ID_QUICK_ADD = 9001
    }
}
