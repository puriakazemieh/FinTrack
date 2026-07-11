package com.kazemieh.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.glass.Switch
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.dashboard_customize_hint
import fintrack.core.designsystem.generated.resources.dashboard_customize_title
import org.jetbrains.compose.resources.stringResource

/**
 * Lets the user reorder (up/down) and show/hide each dashboard widget. Every edit
 * is applied immediately via [onApply], which persists the new layout, so the
 * dashboard updates live behind the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCustomizeSheet(
    items: List<DashboardWidgetItem>,
    onApply: (List<DashboardWidgetItem>) -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    var local by remember { mutableStateOf(items) }

    fun update(newList: List<DashboardWidgetItem>) {
        local = newList
        onApply(newList)
    }

    fun move(from: Int, to: Int) {
        if (to !in local.indices) return
        update(local.toMutableList().apply { add(to, removeAt(from)) })
    }

    fun toggle(index: Int) {
        update(local.toMutableList().apply { this[index] = this[index].copy(visible = !this[index].visible) })
    }

    SheetFrame(
        title = stringResource(Res.string.dashboard_customize_title),
        sub = stringResource(Res.string.dashboard_customize_hint),
        onDismiss = onDismiss,
        isFullScreen = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            local.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { move(index, index - 1) },
                        enabled = index > 0
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = if (index > 0) glassColors.text2 else glassColors.text3.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = { move(index, index + 1) },
                        enabled = index < local.size - 1
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (index < local.size - 1) glassColors.text2 else glassColors.text3.copy(alpha = 0.3f)
                        )
                    }
                    FintrackBodyLargeText(
                        text = stringResource(item.widget.label),
                        modifier = Modifier.weight(1f),
                        color = if (item.visible) glassColors.text else glassColors.text3
                    )
                    Switch(
                        on = item.visible,
                        onToggle = { toggle(index) }
                    )
                }
            }
        }
    }
}
