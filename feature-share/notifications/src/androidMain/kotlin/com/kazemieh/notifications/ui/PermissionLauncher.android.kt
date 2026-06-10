package com.kazemieh.notifications.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = onResult
        )

        LaunchedEffect(trigger) {
            if (trigger) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        LaunchedEffect(trigger) {
            if (trigger) {
                onResult(true)
            }
        }
    }
}
