package com.kazemieh.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import org.jetbrains.compose.resources.decodeToImageBitmap
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassGold
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.lock.LockIntent
import com.kazemieh.lock.LockMode
import com.kazemieh.lock.LockViewModel
import com.kazemieh.lock.PINScreen
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.*
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
    onNavigateToFAQ: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToManageTools: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val uriHandler = LocalUriHandler.current

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

                ProfileEffect.NavigateToOnboarding -> onLoggedOut()
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
                SyncCard(
                    lastSyncTime = state.lastSyncTime,
                    onSyncClick = onNavigateToBackupRestore,
                    isLoading = state.isLoading
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
                        title = stringResource(Res.string.label_currency),
                        icon = Icons.Default.Payments,
                        onClick = onNavigateToCurrencySettings
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_manage_tools),
                        icon = Icons.Default.Build,
                        onClick = onNavigateToManageTools
                    )
                    // Single-option display info rows (no navigation target yet) — shown as
                    // non-interactive value rows rather than dead clickable items.
                    SettingItem(
                        title = stringResource(Res.string.label_language),
                        icon = Icons.Default.Language,
                        value = stringResource(Res.string.label_persian)
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_calendar_fa),
                        icon = Icons.Default.CalendarMonth,
                        value = stringResource(Res.string.label_jalali)
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_text_size),
                        icon = Icons.Default.TextFormat,
                        value = stringResource(Res.string.label_medium)
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
                    SettingItem(
                        title = stringResource(Res.string.setting_hide_balance),
                        icon = Icons.Default.VisibilityOff,
                        on = state.isBalanceHidden,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleHideBalance) }
                    )
                }
            }

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
                }
            }

            item {
                PremiumStatusCard(onRenewClick = { viewModel.onIntent(ProfileIntent.ShowPremiumInfo) })
            }

            item {
                WidgetCard(title = stringResource(Res.string.section_support)) {
                    SettingItem(
                        title = stringResource(Res.string.label_faq),
                        icon = Icons.Default.QuestionMark,
                        onClick = onNavigateToFAQ
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_contact_us),
                        icon = Icons.Default.SupportAgent,
                        onClick = onNavigateToSupport
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_whats_new),
                        icon = Icons.Default.Info,
                        onClick = { uriHandler.openUri(ProfileLinks.RELEASES) }
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_rate_app),
                        icon = Icons.Default.Star,
                        onClick = { uriHandler.openUri(ProfileLinks.RATE) }
                    )
                }
            }

            item {
                WidgetCard(title = stringResource(Res.string.section_about)) {
                    SettingItem(
                        title = stringResource(Res.string.label_terms),
                        icon = Icons.Default.Info,
                        onClick = { uriHandler.openUri(ProfileLinks.TERMS) }
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_privacy),
                        icon = Icons.Default.Security,
                        onClick = { uriHandler.openUri(ProfileLinks.PRIVACY) }
                    )
                    SettingItem(
                        title = stringResource(Res.string.label_version_history),
                        icon = Icons.Default.CalendarMonth,
                        value = ProfileLinks.APP_VERSION
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(space.medium))
                LogoutButton(onClick = { viewModel.onIntent(ProfileIntent.Logout) })
                Spacer(modifier = Modifier.height(space.medium))
                Text(
                    text = stringResource(Res.string.footer_made_with_love),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    val fullName = if (state.firstName.isNotEmpty() || state.lastName.isNotEmpty()) {
        "${state.firstName} ${state.lastName}".trim()
    } else {
        stringResource(Res.string.user_name_default)
    }
    val contactInfo = state.email.ifEmpty { state.phone }.ifEmpty { stringResource(Res.string.user_email_default) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        tone = GlassTone.Strong,
        padding = 16.dp
    ) {
        Column() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.large),
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.large
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.avatar != null) {
                            val bitmap = remember(state.avatar) { state.avatar.decodeToImageBitmap() }
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            FintrackTitleLargeText(
                                text = state.firstName.firstOrNull()?.toString() ?: "پ",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FintrackTitleMediumText(
                            text = fullName,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = GlassGold.copy(alpha = 0.2f),
                            border = border(0.5.dp, GlassGold.copy(alpha = 0.5f), MaterialTheme.shapes.extraSmall)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(10.dp), tint = GlassGold)
                                FintrackLabelSmallText(stringResource(Res.string.label_premium), color = GlassGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    FintrackBodySmallText(text = contactInfo, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GlassGreen))
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.profile_sync_time, state.lastSyncTime),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    onClick = onEditClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProfileStatItem(value = state.transactionCount.toString().toPersianDigits(), label = stringResource(Res.string.profile_stats_transactions))
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                ProfileStatItem(value = state.activeDays.toString().toPersianDigits(), label = stringResource(Res.string.profile_stats_active_days))
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                ProfileStatItem(value = state.toolCount.toString().toPersianDigits(), label = stringResource(Res.string.profile_stats_tools))
            }
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FintrackTitleMediumText(text = value, fontWeight = FontWeight.Bold)
        FintrackLabelSmallText(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SyncCard(
    lastSyncTime: String,
    onSyncClick: () -> Unit,
    isLoading: Boolean
) {
    val space = LocalSpacing.current
    GlassCard(padding = 14.dp) {
        Column() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = GlassGreen.copy(alpha = 0.1f),
                    border = border(1.dp, GlassGreen.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Public, null, modifier = Modifier.size(17.dp), tint = GlassGreen)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FintrackLabelLargeText(text = stringResource(Res.string.profile_sync_active), fontWeight = FontWeight.Bold)
                    FintrackLabelSmallText(text = stringResource(Res.string.profile_sync_time, lastSyncTime), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = GlassGreen.copy(alpha = 0.1f),
                    border = border(1.dp, GlassGreen.copy(alpha = 0.2f), MaterialTheme.shapes.medium),
                    onClick = onSyncClick
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = GlassGreen)
                        }
                        FintrackLabelMediumText(text = stringResource(Res.string.profile_sync_now), color = GlassGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GlassGreen))
                FintrackLabelSmallText(text = stringResource(Res.string.label_google_drive_backup), modifier = Modifier.weight(1f))
                FintrackLabelSmallText(text = stringResource(Res.string.label_backup_size, "—"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PremiumStatusCard(onRenewClick: () -> Unit) {
    GlassCard(padding = 14.dp) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            listOf(GlassGold.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),

            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = GlassAmber
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FintrackLabelLargeText(text = stringResource(Res.string.profile_premium_title), fontWeight = FontWeight.Bold)
                    FintrackLabelSmallText(text = stringResource(Res.string.label_premium_status_summary, "۱۴۰۵/۱۲/۲۹".toPersianDigits(), 9), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = GlassAmber,
                    onClick = onRenewClick
                ) {
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.label_renew),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    value: String? = null,
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyLargeText(text = title)
        }
        if (value != null) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            ) {
                FintrackLabelMediumText(
                    text = value,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (on != null && onToggle != null) {
            Switch(on = on, onToggle = onToggle)
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    val space = LocalSpacing.current
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        border = border(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackBodyLargeText(
                text = stringResource(Res.string.action_logout),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape) =
    androidx.compose.foundation.BorderStroke(width, color)

/** External links and app metadata surfaced from the profile screen. */
private object ProfileLinks {
    const val REPO = "https://github.com/puriakazemieh/FinTrack"
    const val RELEASES = "$REPO/releases"
    const val TERMS = "$REPO/blob/develop/README.md"
    const val PRIVACY = "$REPO/blob/develop/README.md"
    const val RATE = "https://play.google.com/store/apps/details?id=com.kazemieh.fintrack"
    const val APP_VERSION = "۲.۱.۰"
}
