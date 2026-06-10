package com.kazemieh.notifications

interface NotificationManager {
    fun createChannels()
    fun createChannel(id: String, name: String, importance: Int = 3)
    fun showNotification(id: Int, title: String, message: String, channelId: String)
    fun hasPermission(): Boolean
    fun shouldShowRationale(): Boolean
    fun openSettings()

    companion object {
        const val CHANNEL_BUDGET = "budget_reminders"
        const val CHANNEL_INSTALLMENT = "installment_reminders"
        const val CHANNEL_CHEQUE = "cheque_reminders"
    }
}
