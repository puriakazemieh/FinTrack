package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText

/** A single actionable tool shown inside a [ToolGroupSection]. */
data class ToolEntry(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * A titled group of tools rendered as a 3-column grid of glass cards.
 *
 * The section header carries a colored accent dot and the tool icons are tinted
 * with the same [accent], so each category reads as a distinct band while the
 * cards stay visually consistent. Built as a plain (non-lazy) column so several
 * sections can be stacked inside a single scroll container.
 */
@Composable
fun ToolGroupSection(
    title: String,
    accent: Color,
    items: List<ToolEntry>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val glassColors = LocalGlassColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            FintrackLabelLargeText(
                text = title,
                fontWeight = FontWeight.Bold,
                color = glassColors.text
            )
            FintrackLabelMediumText(
                text = items.size.toString(),
                color = glassColors.text3
            )
        }

        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { entry ->
                    ToolCard(
                        entry = entry,
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad the last row so cards keep a consistent width.
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    entry: ToolEntry,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.aspectRatio(1.1f),
        onClick = entry.onClick,
        padding = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accent
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FintrackLabelMediumText(
                text = entry.label,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}
