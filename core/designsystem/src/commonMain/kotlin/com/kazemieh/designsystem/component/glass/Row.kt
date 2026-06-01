package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassHairline
import com.kazemieh.designsystem.GlassText2

/**
 * 2.5 Row — list item inside widgets
 */
@Composable
fun Row(
    label: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    icon: GlassIcon? = null,
    value: @Composable (() -> Unit)? = null,
    badge: @Composable (() -> Unit)? = null,
    divider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon tile
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(it.bg)
                        .border(1.dp, GlassEdge, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = it.painter,
                        contentDescription = null,
                        tint = it.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Label/Sub
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                sub?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassText2,
                        fontSize = 11.sp
                    )
                }
            }

            // Badge
            badge?.invoke()

            // Value
            value?.invoke()
        }
        if (divider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = GlassHairline
            )
        }
    }
}

data class GlassIcon(
    val painter: Painter,
    val bg: Color,
    val color: Color
)
