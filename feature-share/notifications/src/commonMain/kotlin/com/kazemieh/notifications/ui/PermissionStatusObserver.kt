package com.kazemieh.notifications.ui

import androidx.compose.runtime.Composable

/** Null means the platform cannot report that permission; it is never a grant. */
data class PermissionSnapshot(val smsGranted: Boolean? = null, val notificationsGranted: Boolean? = null)

/** Observes existing authorization on entry and resume, without requesting permissions. */
@Composable
expect fun PermissionStatusObserver(onResult: (PermissionSnapshot) -> Unit)
