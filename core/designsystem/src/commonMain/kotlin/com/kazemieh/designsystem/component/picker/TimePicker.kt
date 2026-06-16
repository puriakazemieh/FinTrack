package com.kazemieh.designsystem.component.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.SheetFrame
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import com.kazemieh.designsystem.LocalGlassColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FintrackTimePickerBottomSheet(
    openSheet: MutableState<Boolean>,
    initialTime: String = "00:00",
    onConfirm: (String) -> Unit
) {
    if (openSheet.value) {
        val glassColors = LocalGlassColors.current
        val initialParts = initialTime.split(":")
        val initialHour = initialParts.getOrNull(0)?.toIntOrNull() ?: 0
        val initialMinute = initialParts.getOrNull(1)?.toIntOrNull() ?: 0

        var selectedHour by remember { mutableStateOf(initialHour) }
        var selectedMinute by remember { mutableStateOf(initialMinute) }

        SheetFrame(
            title = stringResource(Res.string.title_select_time),
            onDismiss = { openSheet.value = false },
            isFullScreen = false,
            primaryButtonText = stringResource(Res.string.dp_confirm),
            onPrimaryClick = {
                val time = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                onConfirm(time)
                openSheet.value = false
            },
            secondaryButtonText = stringResource(Res.string.dp_cancel),
            onSecondaryClick = { openSheet.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeColumn(
                        label = stringResource(Res.string.label_minute),
                        range = 0..59,
                        initialValue = initialMinute,
                        onValueChange = { selectedMinute = it }
                    )

                    FintrackDisplaySmallText(
                        text = ":",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)
                    )

                    TimeColumn(
                        label = stringResource(Res.string.label_hour),
                        range = 0..23,
                        initialValue = initialHour,
                        onValueChange = { selectedHour = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    label: String,
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit
) {
    val glassColors = LocalGlassColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FintrackTitleMediumText(text = label, color = glassColors.text3)
        Spacer(modifier = Modifier.height(8.dp))
        
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialValue)
        
        // Simple implementation of a "wheel" using LazyColumn
        // For a more advanced version, we would use snapping and infinite scroll
        Box(
            modifier = Modifier
                .height(150.dp)
                .width(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Highlight background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(glassColors.text.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 50.dp)
            ) {
                items(range.last - range.first + 1) { index ->
                    val value = range.first + index
                    val isSelected = listState.firstVisibleItemIndex == index
                    
                    LaunchedEffect(listState.firstVisibleItemIndex) {
                        onValueChange(listState.firstVisibleItemIndex + range.first)
                    }

                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .clickable {
                                // Scroll to this item
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FintrackTitleLargeText(
                            text = value.toString().padStart(2, '0').toPersianDigits(),
                            color = if (isSelected) com.kazemieh.designsystem.GlassGreen else glassColors.text,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isSelected) 28.sp else 20.sp
                        )
                    }
                }
            }
        }
    }
}
