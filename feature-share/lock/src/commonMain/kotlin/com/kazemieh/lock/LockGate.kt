package com.kazemieh.lock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val snackbarHostState = remember { SnackbarHostState() }

    val biometricAuthenticator = rememberBiometricAuthenticator()
    val biometricTitle = stringResource(Res.string.biometric_title)
    val biometricSubtitle = stringResource(Res.string.biometric_subtitle)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(LockIntent.Init(LockMode.UNLOCK))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LockEffect.TriggerBiometric -> {
                    if (state.isBiometricEnabled && biometricAuthenticator.isBiometricAvailable()) {
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
                is LockEffect.SuccessWithToast -> {
                    com.kazemieh.designsystem.component.SnackbarController.showMessage(effect.message)
                }
                is LockEffect.Error -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isInitialized) {
            if (!state.isLocked) {
                content()
            } else {
                PINScreen(
                    state = state,
                    onIntent = viewModel::onIntent
                )
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
