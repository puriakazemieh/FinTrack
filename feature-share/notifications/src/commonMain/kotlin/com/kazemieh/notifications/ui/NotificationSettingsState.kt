package com.kazemieh.notifications.ui

data class NotificationSettingsState(
    val isBudgetNotifEnabled: Boolean = true,
    val isInstallmentNotifEnabled: Boolean = true,
    val isChequeNotifEnabled: Boolean = true,
    val isFixedExpenseNotifEnabled: Boolean = true,
    val isShoppingNotifEnabled: Boolean = true,
    val isNotesNotifEnabled: Boolean = true,
    val isDebtNotifEnabled: Boolean = true,
    val isQuickAddNotifEnabled: Boolean = false,
    val isQuietHoursEnabled: Boolean = false,
    val quietStart: String = "22:00",
    val quietEnd: String = "08:00",
    val showPermissionRationale: Boolean = false,
    val triggerSystemPermissionRequest: Boolean = false,
    val isLoading: Boolean = false
)
