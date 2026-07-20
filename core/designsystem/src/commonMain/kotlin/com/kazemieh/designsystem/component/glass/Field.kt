package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText3
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * 2.8 Field — glass form row
 */
@Composable
fun Field(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    hint: String? = null,
    error: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingAction: @Composable (() -> Unit)? = null,
    children: @Composable () -> Unit
) {
    val glassColors = LocalGlassColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        padding = 14.dp,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FintrackLabelMediumText(
                        text = label,
                        color = if (error) GlassRed else glassColors.text3
                    )
                    if (required) {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.label_required_marker),
                            color = GlassRed,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    } else if (hint == null) {
                        FintrackLabelSmallText(
                            text = " (${stringResource(Res.string.label_optional)})",
                            color = glassColors.text3.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                    trailingAction?.invoke()
                }
                
                Box {
                    children()
                }
            }
            
            hint?.let {
                FintrackLabelSmallText(
                    text = it,
                    color = glassColors.text3.copy(alpha = 0.65f)
                )
            }
        }
    }
}
