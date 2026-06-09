package com.kazemieh.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.Switch
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateToThemeAndCurrency: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
//    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative background blobs
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(vertical = space.large)
        ) {
            item {
                ProfileHero(onEditClick = onNavigateToProfileEdit)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space.medium)
                ) {
                    SyncCard(modifier = Modifier.weight(1f))
                    PremiumCard(modifier = Modifier.weight(1f))
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.section_display)) {
                    SettingItem(
                        title = stringResource(Res.string.setting_theme_currency),
                        icon = Icons.Default.Palette,
                        onClick = onNavigateToThemeAndCurrency
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
                SettingsSection(title = stringResource(Res.string.section_security)) {
                    SettingItem(
                        title = "قفل برنامه",
                        icon = Icons.Default.Lock,
                        on = state.isLockEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleLock) }
                    )
                    SettingItem(
                        title = stringResource(Res.string.setting_fingerprint),
                        icon = Icons.Default.Fingerprint,
                        on = state.isFingerprintEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleFingerprint) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.section_data)) {
                    SettingItem(
                        title = stringResource(Res.string.setting_backup),
                        icon = Icons.Default.Backup,
                        on = state.isBackupEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleBackup) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.section_notifications)) {
                    SettingItem(
                        title = stringResource(Res.string.setting_push_notifications),
                        icon = Icons.Default.Notifications,
                        on = state.isPushNotificationsEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.TogglePushNotifications) }
                    )
                    SettingItem(
                        title = stringResource(Res.string.setting_transaction_alerts),
                        icon = Icons.Default.Warning,
                        on = state.isTransactionAlertsEnabled,
                        onToggle = { viewModel.onIntent(ProfileIntent.ToggleTransactionAlerts) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(space.medium))
                LogoutButton(onClick = { viewModel.onIntent(ProfileIntent.Logout) })
                Spacer(modifier = Modifier.height(space.extraLarge))
            }
        }
    }
}

@Composable
fun ProfileHero(
    onEditClick: () -> Unit
) {
    val space = LocalSpacing.current
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
                FintrackHeadlineLargeText(
                    text = stringResource(Res.string.placeholder_user_initial),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(space.medium))
        FintrackTitleLargeText(
            text = stringResource(Res.string.user_name_default),
            fontWeight = FontWeight.Bold
        )
        FintrackBodyMediumText(
            text = stringResource(Res.string.user_email_default),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SyncCard(modifier: Modifier = Modifier) {
    val space = LocalSpacing.current
    GlassCard(modifier = modifier) {
        Column {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(space.small))
            FintrackLabelLargeText(
                text = stringResource(Res.string.profile_sync_title),
                fontWeight = FontWeight.Bold
            )
            FintrackLabelSmallText(
                text = stringResource(Res.string.profile_sync_desc, "۱۰ دقیقه پیش"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PremiumCard(modifier: Modifier = Modifier) {
    val space = LocalSpacing.current
    GlassCard(modifier = modifier) {
        Column {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(space.small))
            FintrackLabelLargeText(
                text = stringResource(Res.string.profile_premium_title),
                fontWeight = FontWeight.Bold
            )
            FintrackLabelSmallText(
                text = stringResource(Res.string.profile_premium_desc),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSection(
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
                imageVector = Icons.Default.ChevronRight,
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
