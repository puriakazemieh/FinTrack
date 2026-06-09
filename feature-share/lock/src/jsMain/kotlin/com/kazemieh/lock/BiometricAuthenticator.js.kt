package com.kazemieh.lock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class JsBiometricAuthenticator : BiometricAuthenticator {
    override fun isBiometricAvailable(): Boolean = false
    override fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        onError("Biometrics not supported on Web")
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    return remember { JsBiometricAuthenticator() }
}
