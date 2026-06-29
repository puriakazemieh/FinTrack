package com.kazemieh.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors

@Composable
fun CircularProgress(
    progress: Float,
    modifier: Modifier = Modifier.size(116.dp)
) {
    val glassColors = LocalGlassColors.current
    Canvas(modifier = modifier) {
        drawCircle(
            color = glassColors.text.copy(alpha = 0.1f),
            style = Stroke(width = 4.dp.toPx())
        )
        drawArc(
            color = GlassGreen,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
