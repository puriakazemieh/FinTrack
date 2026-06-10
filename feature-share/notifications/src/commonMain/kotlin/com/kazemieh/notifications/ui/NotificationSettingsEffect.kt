package com.kazemieh.notifications.ui

sealed interface NotificationSettingsEffect {
    data object RequestNotificationPermission : NotificationSettingsEffect
    data class ShowMessage(val message: String) : NotificationSettingsEffect
}
