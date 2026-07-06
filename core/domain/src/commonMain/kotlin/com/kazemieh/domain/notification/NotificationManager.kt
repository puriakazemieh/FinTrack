package com.kazemieh.domain.notification

interface NotificationManager {
    fun createChannels()
    fun createChannel(id: String, name: String, importance: Int = 3)
    fun showNotification(id: Int, title: String, message: String, channelId: String)
    fun hasPermission(): Boolean
    fun openSettings()
    fun showStickyNotification(id: Int, title: String, message: String)
    fun showBudgetAlert(categoryId: Int, categoryName: String, progressPercentage: Int)

    /** Shows a persistent one-tap "add transaction" notification, deep-linking into the add sheet. */
    fun showQuickAddNotification(title: String, message: String, actionLabel: String)
    fun cancelNotification(id: Int)

    companion object {
        const val CHANNEL_BUDGET = "budget_reminders"
        const val CHANNEL_INSTALLMENT = "installment_reminders"
        const val CHANNEL_CHEQUE = "cheque_reminders"
        const val CHANNEL_SHOPPING = "shopping_reminders"
        const val CHANNEL_NOTE = "note_reminders"
        const val CHANNEL_SMS = "sms_detection"
        const val CHANNEL_QUICK_ADD = "quick_add"
        const val ID_QUICK_ADD = 9001
    }
}
