package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun PermissionStatusObserver(onResult: (PermissionSnapshot) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val report by rememberUpdatedState(onResult)
    DisposableEffect(owner) {
        var active = true
        var generation = 0
        fun refresh() {
            val request = ++generation
            UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
                scope.launch {
                    if (active && generation == request) {
                        val authorized = settings.authorizationStatus in setOf(
                            UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral)
                        report(PermissionSnapshot(smsGranted = false, notificationsGranted = authorized))
                    }
                }
            }
        }
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        owner.lifecycle.addObserver(observer)
        refresh()
        onDispose { active = false; owner.lifecycle.removeObserver(observer) }
    }
}
