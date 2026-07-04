package com.kazemieh.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.notifications.ui.NotificationPermissionLauncher
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val ONBOARDING_TOTAL_STEPS = 3

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
                    CircularProgressIndicator(color = GlassGreen)
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
                    OnboardingProgress(
                        currentStep = state.currentStep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                    )

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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.currentStep > 1) {
                            SecondaryPillButton(
                                text = stringResource(Res.string.onboarding_previous),
                                onClick = { viewModel.onIntent(OnboardingIntent.PreviousStep) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        PrimaryPillButton(
                            text = if (state.currentStep == ONBOARDING_TOTAL_STEPS) {
                                stringResource(Res.string.onboarding_start)
                            } else {
                                stringResource(Res.string.onboarding_next)
                            },
                            onClick = { viewModel.onIntent(OnboardingIntent.NextStep) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgress(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(ONBOARDING_TOTAL_STEPS) { index ->
            val active = index < currentStep
            val fraction by animateFloatAsState(
                targetValue = if (active) 1f else 0f,
                animationSpec = tween(250)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(glassColors.glassHairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(GlassGreen)
                )
            }
        }
    }
}

@Composable
private fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        FintrackBodyLargeText(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SecondaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(99.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        FintrackBodyLargeText(
            text = text,
            color = glassColors.text,
            fontWeight = FontWeight.Bold
        )
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
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(120.dp)
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(GlassGreen.copy(alpha = 0.14f))
            .border(1.dp, GlassGreen.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        FintrackHeadlineMediumText(
            text = "پ",
            color = GlassGreen,
            fontWeight = FontWeight.Bold
        )
    }

    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_welcome_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_welcome_desc),
        textAlign = TextAlign.Center,
        color = glassColors.text3
    )

    Spacer(modifier = Modifier.height(32.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureItem(stringResource(Res.string.onboarding_feature_sms_title), Icons.Default.Sms)
        FeatureItem(stringResource(Res.string.onboarding_feature_budget_title), Icons.Default.Notifications)
        FeatureItem(stringResource(Res.string.onboarding_feature_sync_title), Icons.Default.Sync)
    }
}

@Composable
fun FeatureItem(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassGreen,
            modifier = Modifier.size(24.dp)
        )
        FintrackBodyLargeText(text = text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PermissionsStep() {
    val glassColors = LocalGlassColors.current
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
        color = glassColors.text3
    )

    Spacer(modifier = Modifier.height(32.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_sms_title),
            desc = stringResource(Res.string.onboarding_perm_sms_desc),
            icon = Icons.Default.Sms,
            color = GlassGreen
        )
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_notif_title),
            desc = stringResource(Res.string.onboarding_perm_notif_desc),
            icon = Icons.Default.Notifications,
            color = GlassAmber,
            onToggle = { triggerPermission = true }
        )
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_biometric_title),
            desc = stringResource(Res.string.onboarding_perm_biometric_desc),
            icon = Icons.Default.Fingerprint,
            color = GlassBlue
        )
    }
}

@Composable
fun PermissionToggleItem(
    title: String,
    desc: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onToggle: (Boolean) -> Unit = {}
) {
    val glassColors = LocalGlassColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f))
                .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyLargeText(text = title, fontWeight = FontWeight.Bold)
            FintrackBodySmallText(text = desc, color = glassColors.text3)
        }

        var checked by remember { mutableStateOf(false) }
        Switch(
            on = checked,
            onToggle = {
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

    FintrackOutlinedTextField(
        value = name,
        onValueChange = { onUpdate(it, balance) },
        label = { FintrackBodyMediumText(stringResource(Res.string.onboarding_setup_source_name)) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    FintrackOutlinedTextField(
        value = balance,
        onValueChange = { onUpdate(name, it) },
        label = { FintrackBodyMediumText(stringResource(Res.string.onboarding_setup_balance)) },
        modifier = Modifier.fillMaxWidth(),
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
    FintrackOutlinedTextField(
        value = securityQuestion,
        onValueChange = { onUpdateSecurity(it, securityAnswer) },
        label = { FintrackBodySmallText(stringResource(Res.string.onboarding_setup_security_hint)) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    FintrackOutlinedTextField(
        value = securityAnswer,
        onValueChange = { onUpdateSecurity(securityQuestion, it) },
        label = { FintrackBodySmallText(stringResource(Res.string.onboarding_setup_security_answer)) },
        modifier = Modifier.fillMaxWidth()
    )
}
