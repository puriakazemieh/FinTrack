package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors

@Composable
fun FintrackScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    sub: String? = null,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    actions: List<HeaderAction> = emptyList(),
    center: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FintrackBackgroundBlobs()

        Column(modifier = Modifier.fillMaxSize()) {
            if (title != null) {
                ScreenHeader(
                    title = title,
                    sub = sub,
                    onBack = onBack,
                    onClose = onClose,
                    actions = actions,
                    center = center,
                    trailingContent = trailingContent
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            floatingActionButton()
        }
    }
}

@Composable
fun FintrackBackgroundBlobs() {
    if (!LocalGlassColors.current.isGlass) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = 400.dp.toPx()
            ),
            center = Offset(size.width * 0.8f, size.height * 0.2f),
            radius = 400.dp.toPx()
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.1f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.8f),
                radius = 500.dp.toPx()
            ),
            center = Offset(size.width * 0.2f, size.height * 0.8f),
            radius = 500.dp.toPx()
        )
    }
}
