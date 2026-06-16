package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import org.koin.compose.viewmodel.koinViewModel

import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetActionsSheet(
    asset: Asset,
    onDismiss: () -> Unit,
    onEdit: (Asset) -> Unit,
    onViewHistory: (Asset) -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FintrackTitleLargeText(
                    text = asset.name,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ActionItem(
                    icon = Icons.Default.Edit,
                    title = stringResource(Res.string.action_edit_asset),
                    onClick = {
                        onEdit(asset)
                        onDismiss()
                    }
                )

                ActionItem(
                    icon = Icons.Default.History,
                    title = stringResource(Res.string.action_view_history),
                    onClick = {
                        onViewHistory(asset)
                        onDismiss()
                    }
                )

                ActionItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(Res.string.action_delete_asset),
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        viewModel.onIntent(AssetIntent.DeleteAsset(asset.id ?: 0L))
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = color)
        FintrackBodyLargeText(text = title, color = color)
    }
}
