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
        MaterialTheme.colorScheme.surfaceVariant 
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) // GlassStrong fallback
    }
    
    val borderColor = if (tone == GlassTone.Default) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f) // GlassEdgeStrong fallback
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .glassBlur()
            .padding(padding)
    ) {
        content()
    }
}

enum class GlassTone {
    Default, Strong
}
