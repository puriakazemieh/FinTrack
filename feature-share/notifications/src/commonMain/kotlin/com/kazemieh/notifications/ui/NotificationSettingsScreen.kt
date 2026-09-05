package com.kazemieh.notifications.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackLabelLargeText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.label_quick_add_notification
import fintrack.core.designsystem.generated.resources.notif_budget_label
import fintrack.core.designsystem.generated.resources.notif_fixed_expense_label
import fintrack.core.designsystem.generated.resources.notif_shopping_label
import fintrack.core.designsystem.generated.resources.notif_notes_label
import fintrack.core.designsystem.generated.resources.notif_debt_label
import fintrack.core.designsystem.generated.resources.notif_channels_title
import fintrack.core.designsystem.generated.resources.notif_cheque_label
import fintrack.core.designsystem.generated.resources.notif_installment_label
import fintrack.core.designsystem.generated.resources.notif_permission_rationale
import fintrack.core.designsystem.generated.resources.notif_quiet_end_label
import fintrack.core.designsystem.generated.resources.notif_quiet_hours
import fintrack.core.designsystem.generated.resources.notif_quiet_hours_desc
import fintrack.core.designsystem.generated.resources.notif_quiet_start_label
import fintrack.core.designsystem.generated.resources.setting_push_notifications
import fintrack.core.designsystem.generated.resources.title_notification_settings
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

    FintrackScreen(
        title = stringResource(Res.string.title_notification_settings),
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(bottom = 100.dp) // Matching profile content padding
        ) {
            if (state.showPermissionRationale) {
                item {
                    PermissionRationaleCard(
                        onRequestPermission = {
                            viewModel.onIntent(
                                NotificationSettingsIntent.RequestPermission(
                                    shouldShowRationale
                                )
                            )
                        },
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
                        title = stringResource(Res.string.notif_fixed_expense_label),
                        icon = Icons.Default.Paid,
                        on = state.isFixedExpenseNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleFixedExpenseNotif) }
                    )
                    NotificationSettingItem(
                        title = stringResource(Res.string.notif_shopping_label),
                        icon = Icons.Default.ShoppingCart,
                        on = state.isShoppingNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleShoppingNotif) }
                    )
                    NotificationSettingItem(
                        title = stringResource(Res.string.notif_notes_label),
                        icon = Icons.Default.EventNote,
                        on = state.isNotesNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleNotesNotif) }
                    )
                    NotificationSettingItem(
                        title = stringResource(Res.string.notif_installment_label),
                        icon = Icons.Default.EventRepeat,
                        on = state.isInstallmentNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleInstallmentNotif) }
                    )
                    NotificationSettingItem(
                        title = stringResource(Res.string.notif_debt_label),
                        icon = Icons.Default.SyncAlt,
                        on = state.isDebtNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleDebtNotif) }
                    )
                    NotificationSettingItem(
                        title = stringResource(Res.string.notif_cheque_label),
                        icon = Icons.Default.ConfirmationNumber,
                        on = state.isChequeNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleChequeNotif) }
                    )
                    NotificationSettingItem(
                        title = stringResource(Res.string.label_quick_add_notification),
                        icon = Icons.Default.AddCircle,
                        on = state.isQuickAddNotifEnabled,
                        onToggle = { viewModel.onIntent(NotificationSettingsIntent.ToggleQuickAddNotif) }
                    )
                }
            }

            item {
                WidgetCard(title = stringResource(Res.string.notif_quiet_hours)) {
                    Column(modifier = Modifier.padding(horizontal = space.medium, vertical = space.small)) {
                        FintrackBodySmallText(
                            text = stringResource(Res.string.notif_quiet_hours_desc),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
