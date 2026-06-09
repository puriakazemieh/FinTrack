package com.kazemieh.lock

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LockGate(
    content: @Composable () -> Unit
) {
    val viewModel: LockViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val biometricAuthenticator = rememberBiometricAuthenticator()
    val biometricTitle = stringResource(Res.string.biometric_title)
    val biometricSubtitle = stringResource(Res.string.biometric_subtitle)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LockEffect.TriggerBiometric -> {
                    if (biometricAuthenticator.isBiometricAvailable()) {
                        biometricAuthenticator.authenticate(
                            title = biometricTitle,
                            subtitle = biometricSubtitle,
                            onSuccess = { viewModel.onBiometricSuccess() },
                            onError = { error -> viewModel.onBiometricError(error) }
                        )
                    }
                }
                LockEffect.Success -> {
                    // Handled by state change (isLocked = false)
                }
                is LockEffect.Error -> {
                    // Optional: show snackbar or similar
                }
            }
        }
    }

    if (!state.isLockEnabled || !state.isLocked) {
        content()
    } else {
        PINScreen(
            state = state,
            onIntent = viewModel::onIntent
        )
    }
}
