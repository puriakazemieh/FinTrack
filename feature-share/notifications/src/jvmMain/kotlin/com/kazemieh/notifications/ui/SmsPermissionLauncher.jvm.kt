package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun SmsPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
) {
    // Reading incoming SMS is not available on desktop.
    LaunchedEffect(trigger) {
        if (trigger) {
            onResult(false)
        }
    }
}
