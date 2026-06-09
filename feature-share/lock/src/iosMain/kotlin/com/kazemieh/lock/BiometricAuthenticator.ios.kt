package com.kazemieh.lock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun isBiometricAvailable(): Boolean = false // TODO: Implement using LocalAuthentication
    override fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // TODO: Implement using LocalAuthentication
        onError("Not implemented on iOS yet")
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    return remember { IosBiometricAuthenticator() }
}
