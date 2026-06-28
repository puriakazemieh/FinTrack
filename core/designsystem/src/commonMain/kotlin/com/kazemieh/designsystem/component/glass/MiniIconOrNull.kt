package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MiniIconOrNull(
    iconRes: DrawableResource?,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    if (iconRes == null ) return

    Surface(
        modifier = modifier.size(20.dp),
        shape = CircleShape,
        color = background,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
