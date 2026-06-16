package com.kazemieh.notifications.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val shouldShowRationale = rememberShouldShowNotificationRationale()

    val showTimePicker = remember { mutableStateOf(false) }
    var pickingStartTime by remember { mutableStateOf(true) }

    NotificationPermissionLauncher(
        trigger = state.triggerSystemPermissionRequest,
        onResult = { granted ->
            viewModel.onIntent(NotificationSettingsIntent.OnPermissionResult(granted))
        }
    )

    LaunchedEffect(shouldShowRationale) {
        viewModel.onIntent(NotificationSettingsIntent.RefreshPermissionStatus(shouldShowRationale))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                NotificationSettingsEffect.RequestNotificationPermission -> {
                    // This intent is now handled via state trigger
                }
                is NotificationSettingsEffect.ShowMessage -> {
                }
            }
        }
    }

    FintrackTimePickerBottomSheet(
        openSheet = showTimePicker,
        initialTime = if (pickingStartTime) state.quietStart else state.quietEnd,
        onConfirm = { time ->
            if (pickingStartTime) {
                viewModel.onIntent(NotificationSettingsIntent.SetQuietStart(time))
            } else {
                viewModel.onIntent(NotificationSettingsIntent.SetQuietEnd(time))
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative background blobs for glass effect (matching ProfileEditScreen)
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = 400.dp.toPx()
                ),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = 400.dp.toPx()
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = 500.dp.toPx()
                ),
                center = Offset(size.width * 0.2f, size.height * 0.8f),
                radius = 500.dp.toPx()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            ScreenHeader(
                title = stringResource(Res.string.title_notification_settings),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = space.large),
                verticalArrangement = Arrangement.spacedBy(space.medium),
                contentPadding = PaddingValues(bottom = 100.dp) // Matching profile content padding
            ) {
                if (state.showPermissionRationale) {
                    item {
                        PermissionRationaleCard(
                            onRequestPermission = { viewModel.onIntent(NotificationSettingsIntent.RequestPermission(shouldShowRationale)) },
                            onDismiss = { viewModel.onIntent(NotificationSettingsIntent.DismissPermissionRationale) }
                        )
                    }
                }

                item {
                    WidgetCard(title = stringResource(Res.string.notif_channels_title)) {
                        NotificationSettingItem(
                            title = stringResource(Res.string.notif_budget_label),
                            icon = Icons.Default.AccountBalanceWallet,
                            on = state.isBudgetNotifEnabled,
                            onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleBudgetNotif) }
                        )
                        NotificationSettingItem(
                            title = stringResource(Res.string.notif_installment_label),
                            icon = Icons.Default.EventRepeat,
                            on = state.isInstallmentNotifEnabled,
                            onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleInstallmentNotif) }
                        )
                        NotificationSettingItem(
                            title = stringResource(Res.string.notif_cheque_label),
                            icon = Icons.Default.ConfirmationNumber,
                            on = state.isChequeNotifEnabled,
                            onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleChequeNotif) }
                        )
                    }
                }

                item {
                    WidgetCard(title = stringResource(Res.string.notif_quiet_hours)) {
                        NotificationSettingItem(
                            title = stringResource(Res.string.notif_quiet_hours),
                            icon = Icons.Default.Bedtime,
                            on = state.isQuietHoursEnabled,
                            onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleQuietHours) }
                        )
                        if (state.isQuietHoursEnabled) {
                            TimePickerItem(
                                title = stringResource(Res.string.notif_quiet_start_label),
                                time = state.quietStart,
                                onClick = { 
                                    pickingStartTime = true
                                    showTimePicker.value = true
                                }
                            )
                            TimePickerItem(
                                title = stringResource(Res.string.notif_quiet_end_label),
                                time = state.quietEnd,
                                onClick = { 
                                    pickingStartTime = false
                                    showTimePicker.value = true
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
fun NotificationSettingItem(
    title: String,
    icon: ImageVector,
    on: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val space = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(space.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(space.medium))
        FintrackBodyLargeText(
            text = title,
            modifier = Modifier.weight(1f)
        )
        Switch(on = on, onToggle = onToggle)
    }
}

@Composable
fun TimePickerItem(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = space.small), // Matching profile field padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(
            text = title,
            modifier = Modifier.weight(1f)
        )
        FintrackBodyLargeText(
            text = time.toPersianDigits(),
            color = LocalGlassColors.current.text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PermissionRationaleCard(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val space = LocalSpacing.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        tone = GlassTone.Strong
    ) {
        Column(modifier = Modifier.padding(space.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(space.medium))
                FintrackBodyLargeText(
                    text = stringResource(Res.string.setting_push_notifications),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(space.small))
            FintrackBodyMediumText( //todo use this
                text = stringResource(Res.string.notif_permission_rationale),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(space.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    FintrackLabelLargeText(stringResource(Res.string.cancell_))
                }
                Spacer(modifier = Modifier.width(space.small))
                FintrackButton(
                    text = stringResource(Res.string.confirm),
                    onClick = onRequestPermission
                )
            }
        }
    }
}
