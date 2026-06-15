package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.unit_toman_short
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    title: String? = null
) {
    val glassColors = LocalGlassColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (title != null) {
            FintrackLabelSmallText(
                text = title,
                color = glassColors.text2,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FintrackTitleSmallText(
                text = value.start.toLong().toSignedPersianPrice() + " " + stringResource(Res.string.unit_toman_short),
                color = GlassGreen,
                fontWeight = FontWeight.Bold
            )
            FintrackTitleSmallText(
                text = value.endInclusive.toLong().toSignedPersianPrice() + " " + stringResource(Res.string.unit_toman_short),
                color = GlassGreen,
                fontWeight = FontWeight.Bold
            )
        }

        RangeSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = GlassGreen,
                activeTrackColor = GlassGreen,
                inactiveTrackColor = GlassGreen.copy(alpha = 0.2f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}
