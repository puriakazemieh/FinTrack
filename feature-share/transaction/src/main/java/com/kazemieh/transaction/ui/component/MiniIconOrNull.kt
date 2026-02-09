package com.kazemieh.transaction.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MiniIconOrNull(
    @DrawableRes iconRes: Int?,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    if (iconRes == null || iconRes == 0) return

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