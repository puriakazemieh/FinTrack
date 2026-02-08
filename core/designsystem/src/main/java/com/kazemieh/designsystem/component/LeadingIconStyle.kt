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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.picker.FinTrackCategoryIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.picker.bestOnColor

enum class LeadingIconStyle { Badge, TintOnly }

@Composable
fun FinTrackLeadingIcon(
    colorId: Int?,
    iconId: Int?,
    modifier: Modifier = Modifier,
    style: LeadingIconStyle = LeadingIconStyle.Badge,
    size: Int = 40,      // dp
    iconSize: Int = 20,  // dp
    corner: Int = 14     // dp
) {
    val colors = FinTrackPickerColors.rainbow()

    val pickedColor = remember(colors, colorId) {
        colors.firstOrNull { it.id == colorId } ?: colors.first()
    }
    val pickedIcon = remember(FinTrackCategoryIcons.icons, iconId) {
        FinTrackCategoryIcons.icons.firstOrNull { it.id == iconId } ?: FinTrackCategoryIcons.icons.first()
    }

    when (style) {
        LeadingIconStyle.Badge -> {
            Box(
                modifier = modifier
                    .size(size.dp)
                    .clip(RoundedCornerShape(corner.dp))
                    .background(pickedColor.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(pickedIcon.resId),
                    contentDescription = null,
                    tint = bestOnColor(pickedColor.color),
                    modifier = Modifier.size(iconSize.dp)
                )
            }
        }

        LeadingIconStyle.TintOnly -> {

            Icon(
                painter = painterResource(pickedIcon.resId),
                contentDescription = null,
                tint = pickedColor.color,
                modifier = modifier.size(iconSize.dp)
            )
        }
    }
}
