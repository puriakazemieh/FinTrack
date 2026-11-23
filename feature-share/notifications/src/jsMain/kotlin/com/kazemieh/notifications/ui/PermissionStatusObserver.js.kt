package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun PermissionStatusObserver(onResult: (PermissionSnapshot) -> Unit) {
    LaunchedEffect(Unit) { onResult(PermissionSnapshot(smsGranted = false)) }
}
