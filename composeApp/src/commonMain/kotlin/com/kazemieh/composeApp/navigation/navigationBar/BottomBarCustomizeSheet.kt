package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.composeApp.navigation.Destinations
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.bottombar_available_tabs
import fintrack.core.designsystem.generated.resources.bottombar_customize_hint
import fintrack.core.designsystem.generated.resources.bottombar_customize_title
import fintrack.core.designsystem.generated.resources.bottombar_selected_tabs
import org.jetbrains.compose.resources.stringResource

private const val MAX_TABS = 4

/**
 * Lets the user choose which four destinations occupy the bottom bar and in what
 * order. Selecting fewer/more than four keeps the previous layout until a valid set
 * of four is present; each valid edit is applied immediately via [onApply].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBarCustomizeSheet(
    selected: List<Destinations>,
    onApply: (List<Destinations>) -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    var chosen by remember { mutableStateOf(selected) }

    fun update(newList: List<Destinations>) {
        chosen = newList
        if (newList.size == MAX_TABS) onApply(newList)
    }

    fun move(from: Int, to: Int) {
        if (to !in chosen.indices) return
        update(chosen.toMutableList().apply { add(to, removeAt(from)) })
    }

    val available = Destinations.entries.filter { it !in chosen }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = glassColors.bg0) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = space.large)
                .padding(bottom = space.large)
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
        ) {
            FintrackTitleMediumText(
                text = stringResource(Res.string.bottombar_customize_title),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            FintrackBodyMediumText(
                text = stringResource(Res.string.bottombar_customize_hint),
                color = glassColors.text3
            )

            Spacer(Modifier.height(space.medium))
            FintrackLabelMediumText(
                text = stringResource(Res.string.bottombar_selected_tabs),
                color = glassColors.text2,
                fontWeight = FontWeight.Bold
            )
            chosen.forEachIndexed { index, dest ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { move(index, index - 1) }, enabled = index > 0) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = glassColors.text2)
                    }
                    IconButton(onClick = { move(index, index + 1) }, enabled = index < chosen.size - 1) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = glassColors.text2)
                    }
                    TabIcon(dest, glassColors.accent)
                    FintrackBodyLargeText(
                        text = stringResource(dest.label),
                        modifier = Modifier.weight(1f),
                        color = glassColors.text
                    )
                    // Keep at least one tab; removal is only allowed above the minimum.
                    IconButton(
                        onClick = { update(chosen.toMutableList().apply { removeAt(index) }) },
                        enabled = chosen.size > 1
                    ) {
                        Icon(Icons.Default.Close, null, tint = glassColors.text3)
                    }
                }
            }

            if (available.isNotEmpty()) {
                Spacer(Modifier.height(space.medium))
                FintrackLabelMediumText(
                    text = stringResource(Res.string.bottombar_available_tabs),
                    color = glassColors.text2,
                    fontWeight = FontWeight.Bold
                )
                available.forEach { dest ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabIcon(dest, glassColors.text3)
                        FintrackBodyLargeText(
                            text = stringResource(dest.label),
                            modifier = Modifier.weight(1f),
                            color = glassColors.text2
                        )
                        IconButton(
                            onClick = { update(chosen + dest) },
                            enabled = chosen.size < MAX_TABS
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = if (chosen.size < MAX_TABS) glassColors.accent else glassColors.text3.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIcon(dest: Destinations, tint: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(dest.icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}
