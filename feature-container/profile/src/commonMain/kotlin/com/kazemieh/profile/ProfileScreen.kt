package com.kazemieh.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelLargeText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.lock.LockIntent
import com.kazemieh.lock.LockMode
import com.kazemieh.lock.LockViewModel
import com.kazemieh.lock.PINScreen
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_logout
import fintrack.core.designsystem.generated.resources.currency_rial_full
import fintrack.core.designsystem.generated.resources.currency_toman_full
import fintrack.core.designsystem.generated.resources.label_app_lock
import fintrack.core.designsystem.generated.resources.label_currency
import fintrack.core.designsystem.generated.resources.label_theme
import fintrack.core.designsystem.generated.resources.lock_biometric_backup_pin
import fintrack.core.designsystem.generated.resources.profile_premium_desc
import fintrack.core.designsystem.generated.resources.profile_premium_title
import fintrack.core.designsystem.generated.resources.section_display
import fintrack.core.designsystem.generated.resources.section_notifications
import fintrack.core.designsystem.generated.resources.section_security
import fintrack.core.designsystem.generated.resources.setting_dark_mode
import fintrack.core.designsystem.generated.resources.setting_fingerprint
import fintrack.core.designsystem.generated.resources.setting_push_notifications
import fintrack.core.designsystem.generated.resources.title_notification_settings
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.viewmodel.koinViewModel as lockKoinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToCurrencySettings: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    var showLockSheet by remember { mutableStateOf(false) }
    var lockDialogMode by remember { mutableStateOf(LockMode.CREATE) }
    var shouldTriggerFingerprintAfterSetup by remember { mutableStateOf(false) }

    val lockViewModel: LockViewModel = lockKoinViewModel()
    val lockState by lockViewModel.state.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        viewModel.onIntent(ProfileIntent.Refresh)
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowLockPIN -> {
                    lockDialogMode = effect.mode
                    shouldTriggerFingerprintAfterSetup = effect.triggerFingerprint
                    val subtitle = if (effect.triggerFingerprint) {
                        UiText.StringResourceText(Res.string.lock_biometric_backup_pin)
                    } else null
                    lockViewModel.onIntent(LockIntent.Init(effect.mode, subtitle))
                    showLockSheet = true
                }
            }
        }
    }

    LaunchedEffect(lockState.isLocked) {
        if (!lockState.isLocked && showLockSheet) {
            if (lockDialogMode == LockMode.CREATE || lockDialogMode == LockMode.CONFIRM) {
                viewModel.onIntent(ProfileIntent.SetLockState(true))
                if (shouldTriggerFingerprintAfterSetup) {
                    viewModel.onIntent(ProfileIntent.ToggleFingerprint)
                }
            } else if (lockDialogMode == LockMode.VERIFY_BEFORE_DISABLE) {
                viewModel.onIntent(ProfileIntent.SetLockState(false))
            }
            showLockSheet = false
        }
    }

    if (showLockSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLockSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.background,
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    FintrackBackgroundBlobs()
                    PINScreen(
                        state = lockState,
                        onIntent = lockViewModel::onIntent
                    )
                }
            }
        )
    }

    FintrackScreen {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                ProfileHero(
                    state = state,
                    onEditClick = onNavigateToProfileEdit
                )
            }

            item {
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.onIntent(ProfileIntent.ShowPremiumInfo)
                    }
                )
            }

            item {
                WidgetCard(title = stringResource(Res.string.section_display)) {
                    SettingItem(
                        title = stringResource(Res.string.label_theme),
                        icon = Icons.Default.Palette,
                        onClick = onNavigateToThemeSettings
                    )
                    SettingItem(
                        title = stringResource(Res.string.setting_dark_mode),
                        icon = Icons.Default.DarkMode,
                        on = state.isDarkModeEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleDarkMode) }
                    )
                }
            }

            item {
                WidgetCard(title = stringResource(Res.string.label_currency)) {
                    SettingItem(
                        title = stringResource(Res.string.label_currency),
                        icon = Icons.Default.Payments,
                        onClick = onNavigateToCurrencySettings
                    )
                    Spacer(modifier = Modifier.height(space.small))
                    CurrencyQuickSelector(
                        selectedCurrency = state.selectedCurrency,
                        onCurrencySelected = { viewModel.onIntent(ProfileIntent.SelectCurrency(it)) }
                    )
                }
            }

            item {
                WidgetCard(title = stringResource(Res.string.section_security)) {
                    SettingItem(
                        title = stringResource(Res.string.label_app_lock),
                        icon = Icons.Default.Lock,
                        on = state.isLockEnabled,
                        onToggle = {
                            viewModel.onIntent(ProfileIntent.ToggleLock)
                        }
                    )
                    SettingItem(
                        title = stringResource(Res.string.setting_fingerprint),
                        icon = Icons.Default.Fingerprint,
                        on = state.isFingerprintEnabled,
                        onToggle = {
                            viewModel.onIntent(ProfileIntent.ToggleFingerprint)
                        }
                    )
                }
            }

            /*item {
                WidgetCard(title = stringResource(Res.string.section_data)) {
                    SettingItem(
                        title = stringResource(Res.string.setting_backup),
                        icon = Icons.Default.Backup,
                        on = state.isBackupEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleBackup) }
                    )
                }
            }*/

            item {
                WidgetCard(title = stringResource(Res.string.section_notifications)) {
                    SettingItem(
                        title = stringResource(Res.string.title_notification_settings),
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToNotifications
                    )
                    SettingItem(
                        title = stringResource(Res.string.setting_push_notifications),
                        icon = Icons.Default.FlashOn,
                        on = state.isPushNotificationsEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.TogglePushNotifications) }
                    )
                    /* SettingItem( //todo use this
                         title = stringResource(Res.string.setting_transaction_alerts),
                         icon = Icons.Default.Warning,
                         on = state.isTransactionAlertsEnabled,
                         onToggle = { viewModel.onIntent(ProfileIntent.ToggleTransactionAlerts) }
                     )*/
                }
            }

            item {
                Spacer(modifier = Modifier.height(space.medium))
                // LogoutButton(onClick = { viewModel.onIntent(ProfileIntent.Logout) })
                Spacer(modifier = Modifier.height(space.extraLarge))
            }
        }
    }
}

@Composable
fun ProfileHero(
    state: ProfileState,
    onEditClick: () -> Unit
) {
    val space = LocalSpacing.current
    val fullName = "${state.firstName} ${state.lastName}".trim()
    val contactInfo = state.email.ifEmpty { state.phone }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .padding(vertical = space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state.avatar != null) {
                    val bitmap = remember(state.avatar) { state.avatar.decodeToImageBitmap() }
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().rotate(-90f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        if (fullName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(space.medium))
            FintrackTitleLargeText(
                text = fullName,
                fontWeight = FontWeight.Bold
            )
        }
        if (contactInfo.isNotEmpty()) {
            if (fullName.isEmpty()) {
                Spacer(modifier = Modifier.height(space.medium))
            }
            FintrackBodyMediumText(
                text = contactInfo,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    GlassCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFFFD700).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700)
                    )
                }
            }
            Spacer(modifier = Modifier.width(space.medium))
            Column(modifier = Modifier.weight(1f)) {
                FintrackLabelLargeText(
                    text = stringResource(Res.string.profile_premium_title),
                    fontWeight = FontWeight.Bold
                )
                FintrackLabelSmallText(
                    text = stringResource(Res.string.profile_premium_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CurrencyQuickSelector(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurrencyOption(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.currency_toman_full),
            isSelected = selectedCurrency == Currency.TOMAN,
            onClick = { onCurrencySelected(Currency.TOMAN) }
        )
        CurrencyOption(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.currency_rial_full),
            isSelected = selectedCurrency == Currency.RIAL,
            onClick = { onCurrencySelected(Currency.RIAL) }
        )
    }
}

@Composable
fun CurrencyOption(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.height(40.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            FintrackLabelLargeText(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    on: Boolean? = null,
    onToggle: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val space = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
        if (on != null && onToggle != null) {
            Switch(on = on, onToggle = onToggle)
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    val space = LocalSpacing.current
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
        Spacer(modifier = Modifier.width(space.small))
        FintrackBodyLargeText(text = stringResource(Res.string.action_logout))
    }
}
