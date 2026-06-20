package com.kazemieh.sync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.theme.LocalSpacing
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupRestoreScreen(
    viewModel: SyncViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    val colors = LocalGlassColors.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SyncEffect.ShowMessage -> {
                    SnackbarController.showSnackbar(UiText.DynamicString(effect.message))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(Res.string.backup_restore_title),
                subtitle = stringResource(Res.string.backup_restore_subtitle),
                onBack = onBackClick
            )
        },
        containerColor = colors.bg0
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }

            // Active Status Card
            item {
                ActiveStatusCard(
                    lastBackup = state.lastBackup,
                    txCount = state.stats?.transactionCount ?: 0,
                    sourceCount = state.stats?.sourceCount ?: 0,
                    assetCount = state.stats?.assetCount ?: 0,
                    onBackupNow = { viewModel.onIntent(SyncIntent.BackupNow) }
                )
            }

            // Backup Destinations
            item {
                SectionHeader(text = stringResource(Res.string.backup_destinations))
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupDestinationItem(
                        title = stringResource(Res.string.backup_drive_title),
                        subtitle = stringResource(Res.string.backup_drive_desc),
                        icon = Icons.Default.Language,
                        color = GlassGreen,
                        isActive = state.isGoogleDriveEnabled,
                        onToggle = { viewModel.onIntent(SyncIntent.ToggleGoogleDrive(it)) }
                    )
                    BackupDestinationItem(
                        title = stringResource(Res.string.backup_server_title),
                        subtitle = stringResource(Res.string.backup_server_desc),
                        icon = Icons.Outlined.Cloud,
                        color = GlassBlue,
                        isActive = state.isServerSyncEnabled,
                        onToggle = { }
                    )
                    BackupDestinationItem(
                        title = stringResource(Res.string.backup_local_title),
                        subtitle = stringResource(Res.string.backup_local_desc),
                        icon = Icons.Outlined.Description,
                        color = GlassAmber,
                        isActive = false,
                        onToggle = { }
                    )
                }
            }

            // Saved Backups
            item {
                SectionHeader(text = stringResource(Res.string.backup_saved_list))
            }

            items(state.history) { historyItem ->
                SavedBackupItem(
                    data = BackupItemData(
                        time = historyItem.timestamp.toString(), // TODO: Format date
                        type = historyItem.type.name,
                        size = "—",
                        records = historyItem.recordCount
                    ),
                    onRestore = { viewModel.onIntent(SyncIntent.RestoreBackup(historyItem.timestamp.toString())) }
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    ManualActionButton(
                        text = stringResource(Res.string.backup_export_json),
                        icon = Icons.Default.Backup,
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                    ManualActionButton(
                        text = stringResource(Res.string.backup_import_file),
                        icon = Icons.Outlined.Restore,
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(spacing.large)) }
        }
    }
}

@Composable
fun ActiveStatusCard(
    lastBackup: String,
    txCount: Int,
    sourceCount: Int,
    assetCount: Int,
    onBackupNow: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        tone = "strong"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(GlassGreenSoft, RoundedCornerShape(14.dp))
                        .border(1.dp, GlassGreen.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Shield, null, tint = GlassGreen, modifier = Modifier.size(20.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.backup_active_protection), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(Res.string.backup_last_time, lastBackup),
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.text3,
                        fontSize = 10.5.sp
                    )
                }
                
                Button(
                    onClick = onBackupNow,
                    colors = ButtonDefaults.buttonColors(containerColor = GlassGreen, contentColor = GlassGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(stringResource(Res.string.backup_now), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = glassColors.glassHairline,
                thickness = 1.dp
            )
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(label = stringResource(Res.string.backup_stats_transactions), value = txCount.toString(), modifier = Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(glassColors.glassHairline).align(Alignment.CenterVertically))
                StatItem(label = stringResource(Res.string.backup_stats_sources), value = sourceCount.toString(), modifier = Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(glassColors.glassHairline).align(Alignment.CenterVertically))
                StatItem(label = stringResource(Res.string.backup_stats_assets), value = assetCount.toString(), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    val glassColors = LocalGlassColors.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = glassColors.text3, fontSize = 10.sp)
    }
}

@Composable
fun BackupDestinationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val glassColors = LocalGlassColors.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(11.dp))
                    .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (isActive) {
                        Box(modifier = Modifier.size(6.dp).background(GlassGreen, RoundedCornerShape(99.dp)))
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = glassColors.text3, fontSize = 10.5.sp)
            }
            
            Switch(
                checked = isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GlassGreen,
                    uncheckedThumbColor = glassColors.text2,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}

@Composable
fun SavedBackupItem(data: BackupItemData, onRestore: () -> Unit) {
    val glassColors = LocalGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(glassColors.glass, RoundedCornerShape(11.dp))
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(11.dp))
            .padding(10.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(glassColors.glass, RoundedCornerShape(10.dp))
                .border(1.dp, GlassGreen.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Backup, null, tint = GlassGreen, modifier = Modifier.size(13.dp))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(data.time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${data.type} · ${data.size} · " + stringResource(Res.string.backup_records_count, data.records),
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.text3,
                fontSize = 10.sp
            )
        }
        
        TextButton(
            onClick = onRestore,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = glassColors.text)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Restore, null, modifier = Modifier.size(11.dp))
                Text(stringResource(Res.string.backup_restore_btn), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        
        IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreVert, null, tint = glassColors.text3, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun ManualActionButton(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = glassColors.glass, contentColor = glassColors.text),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassColors.glassEdgeStrong),
        shape = RoundedCornerShape(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, modifier = Modifier.size(15.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    val glassColors = LocalGlassColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = glassColors.text2,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

data class BackupItemData(val time: String, val type: String, val size: String, val records: Int)
