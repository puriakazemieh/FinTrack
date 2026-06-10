package com.kazemieh.notifications.ui

data class NotificationSettingsState(
    val isBudgetNotifEnabled: Boolean = true,
    val isInstallmentNotifEnabled: Boolean = true,
    val isChequeNotifEnabled: Boolean = true,
    val isQuietHoursEnabled: Boolean = false,
    val quietStart: String = "22:00",
    val quietEnd: String = "08:00",
    val showPermissionRationale: Boolean = false,
    val triggerSystemPermissionRequest: Boolean = false,
    val isLoading: Boolean = false
)
