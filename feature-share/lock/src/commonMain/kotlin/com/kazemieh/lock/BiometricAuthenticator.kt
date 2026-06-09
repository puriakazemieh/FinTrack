package com.kazemieh.lock

import androidx.compose.runtime.Composable

interface BiometricAuthenticator {
    fun isBiometricAvailable(): Boolean
    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator
