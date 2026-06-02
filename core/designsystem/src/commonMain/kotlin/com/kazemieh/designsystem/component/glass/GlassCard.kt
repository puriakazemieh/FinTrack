package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 2.3 GlassCard — base surface
 * Frosted Glass effect with balanced blur and transparency.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    tone: GlassTone = GlassTone.Default,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (tone == GlassTone.Default) {
        MaterialTheme.colorScheme.surfaceVariant // This is GlassColor (4.5% alpha)
    } else {
        com.kazemieh.designsystem.GlassStrong // This is 8% alpha
    }

    val borderColor = if (tone == GlassTone.Default) {
        MaterialTheme.colorScheme.outline
    } else {
        com.kazemieh.designsystem.GlassEdgeStrong
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        // Frosted Background Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(22.dp))
                .glassBlur(50.dp) // Reverted to the preferred 50dp blur
        )

        // Sharp Content Layer
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

enum class GlassTone {
    Default, Strong
}
