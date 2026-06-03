package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.asString

/**
 * 2.8 ItemSelected — glass row with checkbox
 */
@Composable
fun ItemSelected(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    item: ItemUi,
    onToggle: () -> Unit
) {
    val space = LocalSpacing.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        onClick = onToggle,
        padding = 10.dp,
        tone = if (isSelected) GlassTone.Strong else GlassTone.Default
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = GlassText3.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.width(space.small))
            if (item.iconId != null || item.colorId != null) {
                FinTrackLeadingIcon(
                    colorId = item.colorId ?: 1,
                    iconId = item.iconId ?: 1,
                    style = LeadingIconStyle.Badge,
                    size = 38.dp,
                    iconSize = 16.dp,
                    corner = 12.dp
                )
                Spacer(modifier = Modifier.width(space.medium))
            }
            FintrackBodyMediumText(
                text = item.title.asString(),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GlassText else GlassText2
            )
        }
    }
}
