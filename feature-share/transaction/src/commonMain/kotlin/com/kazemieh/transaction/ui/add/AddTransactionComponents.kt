package com.kazemieh.transaction.ui.add

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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.amount
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_calculator
import fintrack.core.designsystem.generated.resources.label_optional_fa
import fintrack.core.designsystem.generated.resources.label_required_marker
import fintrack.core.designsystem.generated.resources.type_expense
import fintrack.core.designsystem.generated.resources.type_income
import fintrack.core.designsystem.generated.resources.type_transfer
import fintrack.core.designsystem.generated.resources.sms_ref_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun SmsReferenceBanner(
    draft: SmsDraft,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GlassBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(GlassBlue.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = GlassBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.sms_ref_label, draft.bankName),
                        color = GlassBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FintrackLabelSmallText(
                text = draft.body,
                color = glassColors.text.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GlassSegmentedSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val types = listOf(
        TransactionType.EXPENSE to stringResource(Res.string.type_expense),
        TransactionType.INCOME to stringResource(Res.string.type_income),
        TransactionType.TRANSFER to stringResource(Res.string.type_transfer)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(glassColors.glass, RoundedCornerShape(14.dp))
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(14.dp))
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
                FintrackBodyMediumText(
                    text = label,
                    color = if (active) color else glassColors.text3
                )
            }
        }
    }
}

@Composable
fun LargeAmountCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    onCalcClick: () -> Unit,
    autoFocus: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val focusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (autoFocus && !focusRequested) {
            focusRequester.requestFocus()
            focusRequested = true
        }
    }

    GlassCard(
        modifier = modifier,
        tone = if (isError) GlassTone.Error else GlassTone.Strong,
        padding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.label_required_marker),
                        color = GlassRed,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(glassColors.glass)
                        .border(1.dp, glassColors.glassEdge, RoundedCornerShape(8.dp))
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
                            tint = glassColors.text3,
                            modifier = Modifier.size(11.dp)
                        )
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.label_calculator),
                            color = glassColors.text3
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FintrackOutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.length <= 12) onAmountChange(it) },
                    isPrice = true,
                    isError = isError,
                    enabled = enabled,
                    label = { FintrackBodyMediumText(text = stringResource(Res.string.amount)) },
                    textColor = glassColors.text,
                    containerColor = Color.Transparent,
                    unfocusedBorderColor = glassColors.glassEdge,
                    focusedBorderColor = GlassGreen,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = keyboardActions,
                    suffix = {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.currency_toman),
                            color = glassColors.text3
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
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
