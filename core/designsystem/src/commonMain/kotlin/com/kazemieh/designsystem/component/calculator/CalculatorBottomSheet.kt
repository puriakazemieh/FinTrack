package com.kazemieh.designsystem.component.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.util.CalculatorParser
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.Chip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorBottomSheet(
    initialAmount: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var expression by remember { mutableStateOf(initialAmount) }
    var resultPreview by remember { mutableStateOf("") }

    fun updateResult() {
        if (expression.isEmpty()) {
            resultPreview = ""
            return
        }
        val lastChar = expression.lastOrNull()
        if (lastChar != null && !lastChar.isDigit() && lastChar != '%' && lastChar != '.') {
            // Expression ends with an operator, preview result of what we have so far
            val subExpr = expression.dropLast(1)
            resultPreview = if (subExpr.isNotEmpty()) CalculatorParser.evaluate(subExpr) else ""
        } else {
            resultPreview = CalculatorParser.evaluate(expression)
        }
    }

    val glassColors = LocalGlassColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Display Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FintrackHeadlineSmallText(
                        text = expression.toPersianDigits().ifEmpty { "۰" },
                        color = glassColors.text,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    FintrackBodyMediumText(
                        text = if (resultPreview.isNotEmpty() && resultPreview != expression)
                            resultPreview.toPersianDigits() else "",
                        color = glassColors.text3,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                HorizontalDivider(color = glassColors.glassEdge, thickness = 0.5.dp)

                // Percentage Helpers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("+5%", "+10%", "+15%", "+20%").forEach { percent ->
                        Chip(
                            color = GlassGreen,
                            onClick = {
                                if (expression.isNotEmpty() && expression.last().isDigit()) {
                                    expression += percent
                                    updateResult()
                                }
                            }
                        ) {
                            FintrackLabelMediumText(
                                text = percent.toPersianDigits(),
                                color = GlassGreen
                            )
                        }
                    }
                }

                // Keypad
                val keys = listOf(
                    "AC", "÷", "×", "DEL",
                    "7", "8", "9", "-",
                    "4", "5", "6", "+",
                    "1", "2", "3", "=",
                    "%", "0", ".", "OK"
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keys) { key ->
                        KeyButton(
                            key = key,
                            onClick = {
                                when (key) {
                                    "AC" -> {
                                        expression = ""
                                        resultPreview = ""
                                    }
                                    "DEL" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1)
                                            updateResult()
                                        }
                                    }
                                    "=" -> {
                                        val res = CalculatorParser.evaluate(expression)
                                        if (res != "Error") {
                                            expression = res
                                            resultPreview = ""
                                        }
                                    }
                                    "OK" -> {
                                        val finalResult = CalculatorParser.evaluate(expression)
                                        if (finalResult != "Error") {
                                            onConfirm(finalResult)
                                        }
                                    }
                                    else -> {
                                        val lastChar = expression.lastOrNull()
                                        val isOperator = key in listOf("+", "-", "×", "÷", "%")
                                        val lastIsOperator = lastChar in listOf('+', '-', '×', '÷', '%')
                                        
                                        if (isOperator && (expression.isEmpty() || lastIsOperator)) {
                                            // Don't allow starting with operator or consecutive operators
                                            if (key == "-" && expression.isEmpty()) {
                                                expression += key
                                            }
                                        } else {
                                            expression += key
                                            updateResult()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    key: String,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val isAction = key in listOf("AC", "DEL", "OK", "=")
    val isOperator = key in listOf("+", "-", "×", "÷", "%")
    
    val containerColor = when {
        key == "OK" -> GlassGreen.copy(alpha = 0.9f)
        isAction || isOperator -> glassColors.glass
        else -> Color.Transparent
    }
    
    val textColor = when {
        key == "OK" -> Color.White
        key == "AC" || key == "DEL" -> GlassRed
        isOperator -> GlassGreen
        else -> glassColors.text
    }

    Box(
        modifier = Modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .then(
                if (!isAction && !isOperator) Modifier.border(1.dp, glassColors.glassEdge, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            FintrackLabelMediumText(
                text = key.toPersianDigits(),
                color = textColor,
                fontSize = if (key == "OK") 18.sp else 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
