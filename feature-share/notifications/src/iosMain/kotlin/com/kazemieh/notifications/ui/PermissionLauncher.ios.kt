package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
            ) { granted, _ ->
                onResult(granted)
            }
        }
    }
}
