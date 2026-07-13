package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_optional_fa
import org.jetbrains.compose.resources.stringResource

@Composable
fun SectionContainer(
    title: String,
    sub: String? = null,
    onAddClick: () -> Unit,
    addLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val glassColors = LocalGlassColors.current
    GlassCard(
        modifier = modifier,
        padding = 14.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    FintrackLabelMediumText(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.text
                    )
                    sub?.let {
                        FintrackLabelSmallText(
                            text = it,
                            color = glassColors.text3,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                FintrackLabelSmallText(
                    text = stringResource(Res.string.label_optional_fa),
                    color = glassColors.text3,
                    fontSize = 9.sp
                )
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                content()
                AddChip(label = addLabel, onClick = onAddClick)
            }
        }
    }
}

@Composable
fun AddChip(
    label: String,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val stroke = remember {
        Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = glassColors.glassEdgeStrong,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(99.dp.toPx())
            )
        }
        FintrackLabelSmallText(
            text = label,
            color = glassColors.text2,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RemovableChip(
    label: String,
    color: Color,
    onRemove: () -> Unit,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.33f), CircleShape)
            .clickable(onClick = onRemove)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon?.invoke()
        FintrackLabelSmallText(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp)
        )
    }
}
