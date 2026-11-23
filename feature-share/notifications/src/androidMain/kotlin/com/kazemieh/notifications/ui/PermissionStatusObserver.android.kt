package com.kazemieh.notifications.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
actual fun PermissionStatusObserver(onResult: (PermissionSnapshot) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val report by rememberUpdatedState(onResult)
    DisposableEffect(context, owner) {
        fun refresh() {
            val sms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
            val runtimeNotification = Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            report(PermissionSnapshot(sms, runtimeNotification && NotificationManagerCompat.from(context).areNotificationsEnabled()))
        }
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        owner.lifecycle.addObserver(observer)
        refresh()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
