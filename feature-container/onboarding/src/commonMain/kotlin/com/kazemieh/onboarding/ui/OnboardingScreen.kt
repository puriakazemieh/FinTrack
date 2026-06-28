package com.kazemieh.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.notifications.ui.NotificationPermissionLauncher
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

    FintrackScreen {
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
                        onUpdateSecurity = { question, answer ->
                            viewModel.onIntent(OnboardingIntent.UpdateSecurityDetails(question, answer))
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
}

@Composable
fun StepContent(
    state: OnboardingState,
    onUpdateSource: (String, String) -> Unit,
    onUpdateSecurity: (String, String) -> Unit,
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
                    securityQuestion = state.securityQuestion,
                    securityAnswer = state.securityAnswer,
                    onUpdate = onUpdateSource,
                    onUpdateSecurity = onUpdateSecurity
                )
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                FintrackHeadlineMediumText(
                    text = "F",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_welcome_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_welcome_desc),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureItem(stringResource(Res.string.onboarding_feature_sms_title), Icons.Default.Sms)
        FeatureItem(stringResource(Res.string.onboarding_feature_budget_title), Icons.Default.Notifications)
        FeatureItem(stringResource(Res.string.onboarding_feature_sync_title), Icons.Default.Sync)
    }
}

@Composable
fun FeatureItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        FintrackBodyLargeText(text = text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PermissionsStep() {
    var triggerPermission by remember { mutableStateOf(false) }

    NotificationPermissionLauncher(
        trigger = triggerPermission,
        onResult = { granted ->
            triggerPermission = false
            // Handle result if needed
        }
    )

    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_permissions_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_permissions_desc),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_sms_title),
            desc = stringResource(Res.string.onboarding_perm_sms_desc),
            icon = Icons.Default.Sms,
            color = MaterialTheme.colorScheme.primary
        )
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_notif_title),
            desc = stringResource(Res.string.onboarding_perm_notif_desc),
            icon = Icons.Default.Notifications,
            color = androidx.compose.ui.graphics.Color(0xFFF59E0B), // Amber
            onToggle = { triggerPermission = true }
        )
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_biometric_title),
            desc = stringResource(Res.string.onboarding_perm_biometric_desc),
            icon = Icons.Default.Fingerprint,
            color = androidx.compose.ui.graphics.Color(0xFF3B82F6) // Blue
        )
    }
}

@Composable
fun PermissionToggleItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onToggle: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = MaterialTheme.shapes.medium,
            color = color.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyLargeText(text = title, fontWeight = FontWeight.Bold)
            FintrackBodySmallText(text = desc, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        var checked by remember { mutableStateOf(false) }
        Switch(
            checked = checked, 
            onCheckedChange = { 
                checked = it
                onToggle(it)
            }
        )
    }
}

@Composable
fun SetupStep(
    name: String,
    balance: String,
    securityQuestion: String,
    securityAnswer: String,
    onUpdate: (String, String) -> Unit,
    onUpdateSecurity: (String, String) -> Unit
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
    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = name,
        onValueChange = { onUpdate(it, balance) },
        label = { FintrackBodyMediumText(stringResource(Res.string.onboarding_setup_source_name)) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
    Spacer(modifier = Modifier.height(12.dp))
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

    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(16.dp))

    FintrackBodyMediumText(
        text = stringResource(Res.string.onboarding_setup_security_question),
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = securityQuestion,
        onValueChange = { onUpdateSecurity(it, securityAnswer) },
        label = { FintrackBodySmallText(stringResource(Res.string.onboarding_setup_security_hint)) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = securityAnswer,
        onValueChange = { onUpdateSecurity(securityQuestion, it) },
        label = { FintrackBodySmallText(stringResource(Res.string.onboarding_setup_security_answer)) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
}
