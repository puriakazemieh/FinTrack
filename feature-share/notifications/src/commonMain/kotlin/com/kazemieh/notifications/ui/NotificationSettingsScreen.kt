package com.kazemieh.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.Switch
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    NotificationPermissionLauncher(
        trigger = state.triggerSystemPermissionRequest,
        onResult = { granted ->
            viewModel.onIntent(NotificationSettingsIntent.OnPermissionResult(granted))
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                NotificationSettingsEffect.RequestNotificationPermission -> {
                    // This will be handled by the platform Activity or a platform-specific side effect
                }
                is NotificationSettingsEffect.ShowMessage -> {
                    // Show snackbar if needed, but handled globally via SnackbarController usually
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large)
        ) {
            ScreenHeader(
                title = stringResource(Res.string.title_notification_settings),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(space.medium),
                contentPadding = PaddingValues(vertical = space.medium)
            ) {
                if (state.showPermissionRationale) {
                    item {
                        PermissionRationaleCard(
                            onRequestPermission = { viewModel.onIntent(NotificationSettingsIntent.RequestPermission) },
                            onDismiss = { viewModel.onIntent(NotificationSettingsIntent.DismissPermissionRationale) }
                        )
                    }
                }

                item {
                    NotificationSection(title = stringResource(Res.string.notif_channels_title)) {
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
                    NotificationSection(title = stringResource(Res.string.notif_quiet_hours)) {
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
                                onClick = { /* TODO: Show Time Picker */ }
                            )
                            TimePickerItem(
                                title = stringResource(Res.string.notif_quiet_end_label),
                                time = state.quietEnd,
                                onClick = { /* TODO: Show Time Picker */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val space = LocalSpacing.current
    Column(modifier = Modifier.fillMaxWidth()) {
        FintrackTitleSmallText(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = space.small)
        )
        GlassCard(padding = 0.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
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
            .padding(space.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(
            text = title,
            modifier = Modifier.weight(1f)
        )
        FintrackBodyLargeText(
            text = time,
            color = MaterialTheme.colorScheme.primary,
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
            FintrackBodyMediumText(
                text = stringResource(Res.string.notif_permission_rationale),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(space.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancell_))
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
