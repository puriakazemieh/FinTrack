package com.kazemieh.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.component.*
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToDashboard -> onFinish()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                FintrackBodyLargeText(text = stringResource(Res.string.onboarding_loading))
            }
        } else {
            // Skip button at top right
            TextButton(
                onClick = { viewModel.onIntent(OnboardingIntent.Skip) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                FintrackLabelLargeText(
                    text = stringResource(Res.string.onboarding_skip)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                StepContent(
                    state = state,
                    onUpdateSource = { name, balance ->
                        viewModel.onIntent(OnboardingIntent.UpdateSourceDetails(name, balance))
                    },
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.onIntent(OnboardingIntent.PreviousStep) },
                            modifier = Modifier.weight(1f).height(56.dp).padding(end = 8.dp)
                        ) {
                            FintrackBodyLargeText(text = stringResource(Res.string.onboarding_previous))
                        }
                    }

                    Button(
                        onClick = { viewModel.onIntent(OnboardingIntent.NextStep) },
                        modifier = Modifier.weight(1f).height(56.dp).padding(start = 8.dp)
                    ) {
                        FintrackBodyLargeText(
                            text = if (state.currentStep == 3) {
                                stringResource(Res.string.onboarding_start)
                            } else {
                                stringResource(Res.string.onboarding_next)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepContent(
    state: OnboardingState,
    onUpdateSource: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(targetState = state.currentStep, modifier = modifier) { currentStep ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {
                1 -> WelcomeStep()
                2 -> PermissionsStep()
                3 -> SetupStep(
                    name = state.sourceName,
                    balance = state.sourceBalance,
                    onUpdate = onUpdateSource
                )
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Image(
        painter = painterResource(Res.drawable.ic_source_default), // Placeholder for logo
        contentDescription = null,
        modifier = Modifier.size(120.dp).padding(bottom = 32.dp)
    )
    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_welcome_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_welcome_desc),
        textAlign = TextAlign.Center
    )
}

@Composable
fun PermissionsStep() {
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = null,
        modifier = Modifier.size(80.dp).padding(bottom = 24.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_permissions_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_permissions_desc),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    // Note: Actual permission request logic would go here
    // For now, it's informational as per Step 2 requirement
}

@Composable
fun SetupStep(
    name: String,
    balance: String,
    onUpdate: (String, String) -> Unit
) {
    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_setup_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_setup_desc),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = name,
        onValueChange = { onUpdate(it, balance) },
        label = { FintrackBodyMediumText(stringResource(Res.string.onboarding_setup_source_name)) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = balance,
        onValueChange = { onUpdate(name, it) },
        label = { FintrackBodyMediumText(stringResource(Res.string.onboarding_setup_balance)) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
    )
}
