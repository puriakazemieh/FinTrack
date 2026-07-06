package com.kazemieh.notifications.ui

sealed interface NotificationSettingsIntent {
    data object ToggleBudgetNotif : NotificationSettingsIntent
    data object ToggleInstallmentNotif : NotificationSettingsIntent
    data object ToggleChequeNotif : NotificationSettingsIntent
    data object ToggleQuickAddNotif : NotificationSettingsIntent
    data object ToggleQuietHours : NotificationSettingsIntent
    data class SetQuietStart(val time: String) : NotificationSettingsIntent
    data class SetQuietEnd(val time: String) : NotificationSettingsIntent
    data class RequestPermission(val isRationaleShown: Boolean) : NotificationSettingsIntent
    data class OnPermissionResult(val granted: Boolean) : NotificationSettingsIntent
    data class RefreshPermissionStatus(val shouldShowRationale: Boolean) : NotificationSettingsIntent
    data object DismissPermissionRationale : NotificationSettingsIntent
}
