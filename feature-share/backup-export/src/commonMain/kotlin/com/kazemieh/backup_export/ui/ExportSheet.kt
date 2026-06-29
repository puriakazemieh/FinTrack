package com.kazemieh.backup_export.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExportSheet(
    viewModel: BackupExportViewModel = koinViewModel(),
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    val colors = LocalGlassColors.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BackupExportEffect.ShowMessage -> {
                    SnackbarController.showMessage(UiText.DynamicString(effect.message))
                }
            }
        }
    }

    SheetFrame(
        onDismiss = onClose,
        title = stringResource(Res.string.export_title),
        sub = stringResource(
            Res.string.export_subtitle,
            state.transactionCount,
            state.dateRange.first,
            state.dateRange.second
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(spacing.small))

            // Export Options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportOptionItem(
                    title = stringResource(Res.string.export_fintrack_file),
                    subtitle = stringResource(Res.string.export_fintrack_desc),
                    icon = Icons.Default.Backup,
                    color = GlassGreen,
                    onClick = { viewModel.onIntent(BackupExportIntent.ExportJson) }
                )
                ExportOptionItem(
                    title = stringResource(Res.string.export_excel_title),
                    subtitle = stringResource(Res.string.export_excel_desc),
                    icon = Icons.Default.Description,
                    color = GlassBlue,
                    onClick = { viewModel.onIntent(BackupExportIntent.ExportExcel) }
                )
                ExportOptionItem(
                    title = stringResource(Res.string.export_pdf_title),
                    subtitle = stringResource(Res.string.export_pdf_desc),
                    icon = Icons.Default.PictureAsPdf,
                    color = GlassRed,
                    onClick = { viewModel.onIntent(BackupExportIntent.ExportPdf) }
                )
                ExportOptionItem(
                    title = stringResource(Res.string.export_csv_title),
                    subtitle = stringResource(Res.string.export_csv_desc),
                    icon = Icons.AutoMirrored.Filled.List,
                    color = GlassAmber,
                    onClick = { viewModel.onIntent(BackupExportIntent.ExportCsv) }
                )
            }

            // Range Selection Header
            Text(
                text = stringResource(Res.string.export_range_label),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.text2
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Range Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DateRange.entries.forEach { range ->
                    val label = stringResource(range.labelRes)
                    Chip(
                        active = state.selectedRange == range,
                        color = GlassGreen,
                        onClick = { viewModel.onIntent(BackupExportIntent.ChangeRange(range)) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (state.selectedRange == range) FontWeight.Bold else FontWeight.Medium,
                                color = if (state.selectedRange == range) GlassGreen else colors.text3
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(spacing.large))
        }
    }
}

@Composable
fun ExportOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = glassColors.text
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = glassColors.text3,
                        fontSize = 10.5.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = null,
                tint = glassColors.text3,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
