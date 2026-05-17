package com.kazemieh.designsystem.picker


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource

data class PickableColor(
    val id: Int,
    val color: Color
)

data class PickableIcon(
    val id: Int,
    val resource: DrawableResource,
    val isTintable: Boolean = true
)

data class PickableIconAlternative(
    val id: Int,
    val painter: Painter
)