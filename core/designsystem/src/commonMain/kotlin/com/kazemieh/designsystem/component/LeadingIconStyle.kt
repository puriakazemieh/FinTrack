package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.picker.bestOnColor
import org.jetbrains.compose.resources.painterResource

enum class LeadingIconStyle { Badge, TintOnly }

@Composable
fun FinTrackLeadingIcon(
    colorId: Int?,
    iconId: Int?,
    modifier: Modifier = Modifier,
    style: LeadingIconStyle = LeadingIconStyle.Badge,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    corner: Dp = 14.dp
) {
    val colors = FinTrackPickerColors.rainbow()

    val pickedColor = remember(colors, colorId) {
        colors.firstOrNull { it.id == colorId } ?: colors.first()
    }
    val pickedIcon = remember(iconId) {
        FinTrackIcons.findIcon(iconId)
    }

    when (style) {
        LeadingIconStyle.Badge -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(corner))
                    .background(if (pickedIcon.isTintable) pickedColor.color else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(pickedIcon.resource),
                    contentDescription = null,
                    tint = if (pickedIcon.isTintable) bestOnColor(pickedColor.color) else Color.Unspecified,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        LeadingIconStyle.TintOnly -> {

            Icon(
                painter = painterResource(pickedIcon.resource),
                contentDescription = null,
                tint = if (pickedIcon.isTintable) pickedColor.color else Color.Unspecified,
                modifier = modifier.size(iconSize)
            )
        }
    }
}
