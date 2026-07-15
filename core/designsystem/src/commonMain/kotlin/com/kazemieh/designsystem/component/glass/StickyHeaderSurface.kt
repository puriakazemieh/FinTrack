package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors

/**
 * Shared surface for sticky list headers. Instead of a full-bleed, hard-edged [bg1] bar it renders
 * a floating rounded pill with a lighter (translucent) background and a hairline edge, so the header
 * reads as a soft chip rather than a heavy block. Applied to every sticky header for consistency.
 *
 * Call sites should still add their own inner content padding after this modifier.
 */
@Composable
fun Modifier.stickyHeaderSurface(): Modifier {
    val glassColors = LocalGlassColors.current
    return this
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(glassColors.bg1.copy(alpha = 0.7f))
        .border(1.dp, glassColors.glassHairline, RoundedCornerShape(14.dp))
}
