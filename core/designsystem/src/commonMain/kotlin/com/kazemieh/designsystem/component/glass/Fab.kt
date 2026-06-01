package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDeep

/**
 * 2.8 Fab / FabBtn — floating "add" pill
 */
@Composable
fun Fab(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = GlassGreen
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Brush.linearGradient(listOf(GlassGreen, GlassGreenDeep)))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color(0xFF06281A), // Dark contrast from §2.7
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
                color = Color(0xFF06281A)
            )
        }
    }
}
