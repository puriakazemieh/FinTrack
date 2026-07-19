package com.kazemieh.notifications
import com.kazemieh.domain.notification.NotificationManager

class IosNotificationManager : NotificationManager {
    override fun createChannels() {}
    override fun createChannel(id: String, name: String, importance: Int) {}
    override fun showNotification(id: Int, title: String, message: String, channelId: String) {}
    override fun showStickyNotification(id: Int, title: String, message: String, smsDraftId: Long) {}
    override fun showBudgetAlert(categoryId: Int, categoryName: String, progressPercentage: Int) {}
    override fun showQuickAddNotification(title: String, message: String, actionLabel: String) {}
    override fun cancelNotification(id: Int) {}
    override fun hasPermission(): Boolean = true
    override fun openSettings() {}
}
