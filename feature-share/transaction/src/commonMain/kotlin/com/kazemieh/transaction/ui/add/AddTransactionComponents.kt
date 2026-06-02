package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.amount
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.required_star
import org.jetbrains.compose.resources.stringResource

@Composable
fun GlassSegmentedSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(
        TransactionType.EXPENSE to "خرج",
        TransactionType.INCOME to "درآمد",
        TransactionType.TRANSFER to "انتقال"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassColor, RoundedCornerShape(14.dp))
            .border(1.dp, GlassEdge, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        types.forEach { (type, label) ->
            val active = type == selectedType
            val color = when (type) {
                TransactionType.INCOME -> GlassGreen
                TransactionType.TRANSFER -> GlassBlue
                else -> GlassRed
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (active) Modifier
                            .background(color.copy(alpha = 0.14f))
                            .border(1.dp, color.copy(alpha = 0.33f), RoundedCornerShape(10.dp))
                        else Modifier
                    )
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    color = if (active) color else GlassText3,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun LargeAmountCard(
    amount: String,
    onCalcClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        tone = GlassTone.Strong,
        padding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.amount),
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassText3,
                        fontSize = 11.sp
                    )
                    Text(
                        text = " *",
                        color = GlassRed,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassColor)
                        .border(1.dp, GlassEdge, RoundedCornerShape(8.dp))
                        .clickable(onClick = onCalcClick)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = GlassText3,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "ماشین‌حساب",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassText3,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (amount.isEmpty()) "۰" else amount.toLongOrNull()?.toFa() ?: amount,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassText,
                    letterSpacing = (-0.02).sp
                )
                Text(
                    text = stringResource(Res.string.currency_toman),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassText3,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SectionContainer(
    title: String,
    sub: String? = null,
    onAddClick: () -> Unit,
    addLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassText,
                        fontSize = 12.sp
                    )
                    sub?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassText3,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Text(
                    text = "اختیاری",
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassText3,
                    fontSize = 10.sp
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
                color = GlassEdgeStrong,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(99.dp.toPx())
            )
        }
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = GlassText2,
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
        Text(
            text = label,
            fontSize = 11.sp,
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
