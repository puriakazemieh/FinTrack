package com.kazemieh.designsystem.component.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackDisplaySmallText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.SheetFrame
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.dp_cancel
import fintrack.core.designsystem.generated.resources.dp_confirm
import fintrack.core.designsystem.generated.resources.label_hour
import fintrack.core.designsystem.generated.resources.label_minute
import fintrack.core.designsystem.generated.resources.title_select_time
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FintrackTimePickerBottomSheet(
    openSheet: MutableState<Boolean>,
    initialTime: String = "00:00",
    onConfirm: (String) -> Unit,
) {
    if (!openSheet.value) return

    val initialParts = initialTime.split(":")
    var selectedHour by remember(initialTime) {
        mutableStateOf((initialParts.getOrNull(0)?.toIntOrNull() ?: 0).coerceIn(0, 23))
    }
    var selectedMinute by remember(initialTime) {
        mutableStateOf((initialParts.getOrNull(1)?.toIntOrNull() ?: 0).coerceIn(0, 59))
    }

    SheetFrame(
        title = stringResource(Res.string.title_select_time),
        onDismiss = { openSheet.value = false },
        isFullScreen = false,
        primaryButtonText = stringResource(Res.string.dp_confirm),
        onPrimaryClick = {
            onConfirm("${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}")
            openSheet.value = false
        },
        secondaryButtonText = stringResource(Res.string.dp_cancel),
        onSecondaryClick = { openSheet.value = false },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FintrackDisplaySmallText(
                text = "${selectedHour.toString().padStart(2, '0').toPersianDigits()} : ${selectedMinute.toString().padStart(2, '0').toPersianDigits()}",
                color = GlassGreen,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                TimeSelector(
                    modifier = Modifier.weight(0.9f),
                    label = stringResource(Res.string.label_hour),
                    range = 0..23,
                    value = selectedHour,
                    onValueChange = { selectedHour = it },
                )
                TimeSelector(
                    modifier = Modifier.weight(1.5f),
                    label = stringResource(Res.string.label_minute),
                    range = 0..59,
                    value = selectedMinute,
                    onValueChange = { selectedMinute = it },
                )
            }
        }
    }
}

/** Direct number selection plus one-step increase/decrease controls. */
@Composable
private fun TimeSelector(
    modifier: Modifier,
    label: String,
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val glassColors = LocalGlassColors.current
    val columns = if (range.last <= 23) 4 else 6
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FintrackTitleMediumText(text = label, color = glassColors.text3)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                onValueChange(if (value == range.first) range.last else value - 1)
            }) {
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = glassColors.text)
            }
            FintrackTitleLargeText(
                text = value.toString().padStart(2, '0').toPersianDigits(),
                color = GlassGreen,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = {
                onValueChange(if (value == range.last) range.first else value + 1)
            }) {
                Icon(Icons.Outlined.KeyboardArrowUp, null, tint = glassColors.text)
            }
        }
        range.toList().chunked(columns).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { number ->
                    val selected = number == value
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onValueChange(number) },
                        contentAlignment = Alignment.Center,
                    ) {
                        FintrackLabelSmallText(
                            text = number.toString().padStart(2, '0').toPersianDigits(),
                            color = if (selected) GlassGreen else glassColors.text2,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                        )
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.width(26.dp)) }
            }
        }
    }
}
