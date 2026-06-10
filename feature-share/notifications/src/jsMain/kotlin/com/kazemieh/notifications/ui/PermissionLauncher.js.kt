package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            onResult(true)
        }
    }
}
