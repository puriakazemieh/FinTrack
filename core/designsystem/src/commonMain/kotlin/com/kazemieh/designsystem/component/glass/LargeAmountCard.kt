package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.amount
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_calculator
import fintrack.core.designsystem.generated.resources.label_required_marker
import org.jetbrains.compose.resources.stringResource

@Composable
fun LargeAmountCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    onCalcClick: () -> Unit,
    label: String? = null,
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
                    label = { FintrackBodyMediumText(text = label ?: stringResource(Res.string.amount)) },
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
