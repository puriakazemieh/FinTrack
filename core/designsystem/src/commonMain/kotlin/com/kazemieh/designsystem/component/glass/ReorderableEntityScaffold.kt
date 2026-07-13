package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_done
import fintrack.core.designsystem.generated.resources.reorder_list
import org.jetbrains.compose.resources.stringResource

/**
 * Shared scaffold for entity-list screens that support drag-to-reorder.
 *
 * Encapsulates the "reorder mode + overflow menu + Done action" chrome that was
 * previously copy-pasted across Persons/Sources/Categories/Tags screens, so the
 * four screens stay visually and behaviourally identical.
 *
 * When [isReorderMode] is active a green "Done" header action is shown that calls
 * [onConfirmReorder]; otherwise an overflow menu offers a "reorder" entry that
 * calls [onStartReorder]. The actual list (typically an [EntityList] in reorder
 * mode) is provided via [content].
 */
@Composable
fun ReorderableEntityScaffold(
    title: String,
    onBack: () -> Unit,
    isReorderMode: Boolean,
    onStartReorder: () -> Unit,
    onConfirmReorder: () -> Unit,
    sub: String? = null,
    content: @Composable () -> Unit
) {
    FintrackScreen(
        title = title,
        sub = sub,
        onBack = onBack,
        actions = if (isReorderMode) {
            listOf(
                HeaderAction(
                    icon = rememberVectorPainter(Icons.Default.Check),
                    label = stringResource(Res.string.label_done),
                    onClick = onConfirmReorder,
                    color = GlassGreen
                )
            )
        } else {
            emptyList()
        },
        trailingContent = {
            if (!isReorderMode) {
                var showMenu by remember { mutableStateOf(false) }
                val glassColors = LocalGlassColors.current
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = glassColors.text2)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = glassColors.bg1,
                        border = BorderStroke(1.dp, glassColors.glassEdge),
                        tonalElevation = 0.dp,
                        shadowElevation = 12.dp
                    ) {
                        DropdownMenuItem(
                            text = { FintrackBodyMediumText(text = stringResource(Res.string.reorder_list)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Reorder,
                                    contentDescription = null,
                                    tint = GlassGreen
                                )
                            },
                            colors = MenuDefaults.itemColors(textColor = glassColors.text),
                            onClick = {
                                onStartReorder()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    ) {
        content()
    }
}
