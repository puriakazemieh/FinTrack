package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable

@Composable
expect fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
)
