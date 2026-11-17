package com.kazemieh.composeApp

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// Retain warm deliveries while the lock gate has temporarily removed the navigation host.
// Initial Activity intents are already consumed by Navigation when its graph is first set.
private val incomingDeepLinks = Channel<Intent>(Channel.UNLIMITED)

fun dispatchIncomingDeepLink(intent: Intent) {
    if (intent.action == Intent.ACTION_VIEW && intent.data?.scheme == "fintrack") {
        incomingDeepLinks.trySend(Intent(intent))
    }
}

@Composable
internal actual fun BindIncomingDeepLinks(navController: NavHostController) {
    LaunchedEffect(navController) {
        incomingDeepLinks.receiveAsFlow().collect { navController.handleDeepLink(it) }
    }
}
