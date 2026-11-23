package com.kazemieh.notifications.ui

import android.Manifest
import android.os.Build
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> onResult(granted && NotificationManagerCompat.from(context).areNotificationsEnabled()) }
        )

        LaunchedEffect(trigger) {
            if (trigger) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        LaunchedEffect(trigger) {
            if (trigger) {
                val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (!enabled) {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                onResult(enabled)
            }
        }
    }
}
