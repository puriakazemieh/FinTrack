package com.kazemieh.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
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
import com.kazemieh.notifications.ui.SmsPermissionLauncher
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val ONBOARDING_TOTAL_STEPS = 4

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
                is OnboardingEffect.ShowError -> {
                    // We could show a Snackbar here.
                    // For now, let's just log it or rely on the user seeing the loading stop.
                }
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
                        onSmsReadingResult = { granted ->
                            viewModel.onIntent(OnboardingIntent.SetSmsReading(granted))
                        },
                        onNotificationPermissionResult = { granted ->
                            viewModel.onIntent(OnboardingIntent.NotificationPermissionResult(granted))
                        },
                        onNotificationPermissionRequest = {
                            viewModel.onIntent(OnboardingIntent.NotificationPermissionRequested)
                        },
                        onSelectTheme = { theme ->
                            viewModel.onIntent(OnboardingIntent.SelectTheme(theme))
                        },
                        onSelectAccent = { accent ->
                            viewModel.onIntent(OnboardingIntent.SelectAccent(accent))
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
    onSmsReadingResult: (Boolean) -> Unit,
    onNotificationPermissionResult: (Boolean) -> Unit,
    onNotificationPermissionRequest: () -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectAccent: (String) -> Unit,
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
                2 -> ThemeSelectionStep(
                    selectedTheme = state.selectedTheme,
                    onSelectTheme = onSelectTheme
                )
                3 -> PermissionsStep(
                    onSmsReadingResult = onSmsReadingResult,
                    onNotificationPermissionResult = onNotificationPermissionResult,
                    onNotificationPermissionRequest = onNotificationPermissionRequest
                )
                4 -> SetupStep(
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
            text = stringResource(Res.string.default_profile_initial),
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
fun ThemeSelectionStep(
    selectedTheme: String,
    onSelectTheme: (String) -> Unit
) {
    val glassColors = LocalGlassColors.current

    FintrackHeadlineMediumText(
        text = stringResource(Res.string.onboarding_theme_title),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    FintrackBodyLargeText(
        text = stringResource(Res.string.onboarding_theme_desc),
        textAlign = TextAlign.Center,
        color = glassColors.text3
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Theme cards - 2x2 grid
    val themes = listOf(
        "GLASS_DARK" to Res.string.onboarding_theme_glass_dark,
        "GLASS_LIGHT" to Res.string.onboarding_theme_glass_light,
        "PLAIN_DARK" to Res.string.onboarding_theme_plain_dark,
        "PLAIN_LIGHT" to Res.string.onboarding_theme_plain_light
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        themes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (themeKey, labelRes) ->
                    val isSelected = selectedTheme == themeKey
                    val isDark = themeKey.contains("DARK")
                    val bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF0F4F3)
                    val textColor = if (isDark) Color.White else Color(0xFF06100E)
                    val borderColor = if (isSelected) GlassGreen else glassColors.glassHairline

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTheme(themeKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FintrackBodyMediumText(
                                text = stringResource(labelRes),
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GlassGreen)
                                )
                            }
                        }
                    }
                }
            }
        }
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
fun PermissionsStep(
    onSmsReadingResult: (Boolean) -> Unit = {},
    onNotificationPermissionResult: (Boolean) -> Unit = {},
    onNotificationPermissionRequest: () -> Unit = {}
) {
    val glassColors = LocalGlassColors.current
    var triggerPermission by remember { mutableStateOf(false) }
    var triggerSmsPermission by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var smsReadingEnabled by remember { mutableStateOf(false) }

    NotificationPermissionLauncher(
        trigger = triggerPermission,
        onResult = { granted ->
            triggerPermission = false
            notificationsEnabled = granted
            onNotificationPermissionResult(granted)
        }
    )

    SmsPermissionLauncher(
        trigger = triggerSmsPermission,
        onResult = { granted ->
            triggerSmsPermission = false
            smsReadingEnabled = granted
            onSmsReadingResult(granted)
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
            color = GlassGreen,
            checked = smsReadingEnabled,
            onToggle = { enabled ->
                if (enabled) triggerSmsPermission = true else {
                    smsReadingEnabled = false
                    onSmsReadingResult(false)
                }
            }
        )
        PermissionToggleItem(
            title = stringResource(Res.string.onboarding_perm_notif_title),
            desc = stringResource(Res.string.onboarding_perm_notif_desc),
            icon = Icons.Default.Notifications,
            color = GlassAmber,
            checked = notificationsEnabled,
            onToggle = { enabled ->
                if (enabled) {
                    onNotificationPermissionRequest()
                    triggerPermission = true
                } else {
                    notificationsEnabled = false
                    onNotificationPermissionResult(false)
                }
            }
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
    checked: Boolean = false,
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

        Switch(
            on = checked,
            onToggle = onToggle
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
}
