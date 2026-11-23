@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Shared management actions; picker rows keep showActions=false at their owner. */
@Composable
fun EntityActionsBottomSheet(
    name: String,
    isDefault: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: (() -> Unit)? = null,
    onFilter: (() -> Unit)? = null
) {
    fun perform(action: () -> Unit) { onDismiss(); action() }
    SheetFrame(title = name, onDismiss = onDismiss) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!isDefault) onSetDefault?.let { action ->
                EntityAction(stringResource(Res.string.entity_set_default), Icons.Default.StarBorder) { perform(action) }
            }
            onFilter?.let { action ->
                EntityAction(stringResource(Res.string.entity_view_transactions), Icons.Default.FilterList) { perform(action) }
            }
            EntityAction(stringResource(Res.string.edit), Icons.Default.Edit) { perform(onEdit) }
            if (!isDefault) EntityAction(stringResource(Res.string.delete), Icons.Default.Delete, destructive = true) { perform(onDelete) }
        }
    }
}

@Composable
private fun EntityAction(label: String, icon: ImageVector, destructive: Boolean = false, onClick: () -> Unit) {
    val color = if (destructive) MaterialTheme.colorScheme.error else LocalGlassColors.current.text
    GlassCard(padding = 0.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 48.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            FintrackBodyLargeText(label, color = color, modifier = Modifier.weight(1f))
        }
    }
}
