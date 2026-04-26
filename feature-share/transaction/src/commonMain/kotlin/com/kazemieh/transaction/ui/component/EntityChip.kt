package com.kazemieh.transaction.ui.component


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import org.jetbrains.compose.resources.DrawableResource


@Composable
fun EntityChip(
    name: String,
    iconRes: DrawableResource?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100),
        color = tint.copy(alpha = 0.10f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniIconOrNull(
                iconRes = iconRes,
                tint = tint,
                background = Color.Transparent,
                modifier = Modifier.size(16.dp)
            )
            if (iconRes != null ) {
                Spacer(modifier = Modifier.width(6.dp))
            }
            FintrackLabelSmallText(
                text = name,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}