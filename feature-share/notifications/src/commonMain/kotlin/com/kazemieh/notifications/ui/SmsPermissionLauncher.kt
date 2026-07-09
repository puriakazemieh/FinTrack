package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable

/**
 * Requests the runtime permission needed to read incoming bank SMS (RECEIVE_SMS on Android).
 * Flip [trigger] to true to launch the request; [onResult] reports whether it was granted.
 * On platforms without SMS support the result is reported as false.
 */
@Composable
expect fun SmsPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
)
