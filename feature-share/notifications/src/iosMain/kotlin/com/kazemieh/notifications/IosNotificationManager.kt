package com.kazemieh.notifications

class IosNotificationManager : NotificationManager {
    override fun createChannels() {}
    override fun createChannel(id: String, name: String, importance: Int) {}
    override fun showNotification(id: Int, title: String, message: String, channelId: String) {}
    override fun hasPermission(): Boolean = true
    override fun openSettings() {}
}
