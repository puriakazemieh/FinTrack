package com.kazemieh.designsystem.picker

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class PickableColor(
    val id: Int,
    val color: Color
)

data class PickableIcon(
    val id: Int,
    @DrawableRes val resId: Int
)
