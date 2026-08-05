package com.kazemieh.installment.ui.add.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.*
import androidx.compose.ui.text.input.VisualTransformation
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoanCalculatorCard(
    amount: String,
    installment: String,
    count: String,
    totalPayment: Long,
    totalInterest: Long,
    onAmountChange: (String) -> Unit,
    onInstallmentChange: (String) -> Unit,
    onCountChange: (String) -> Unit,
    onApply: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    var expanded by remember { mutableStateOf(false) }

    GlassCard(padding = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Calculate, null, tint = GlassGreen, modifier = Modifier.size(20.dp))
                    FintrackLabelMediumText(text = "محاسبه‌گر وام", fontWeight = FontWeight.Bold)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = glassColors.text3
                )
            }

            if (expanded) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LoanField(
                            value = amount,
                            label = "مبلغ وام",
                            onValueChange = onAmountChange,
                            modifier = Modifier.weight(1f),
                            visualTransformation = NumberCommaTransformation()
                        )
                        LoanField(
                            value = count,
                            label = stringResource(Res.string.total_installments),
                            onValueChange = onCountChange,
                            modifier = Modifier.weight(0.6f),
                            visualTransformation = PersianNumberTransformation()
                        )
                    }
                    LoanField(
                        value = installment,
                        label = stringResource(Res.string.installment_amount),
                        onValueChange = onInstallmentChange,
                        visualTransformation = NumberCommaTransformation()
                    )

                    HorizontalDivider(color = glassColors.glassEdge, thickness = 1.dp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            FintrackLabelSmallText(text = "کل بازپرداخت", color = glassColors.text3)
                            FintrackLabelMediumText(text = totalPayment.toPersianPrice() + " " + stringResource(Res.string.currency_toman), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            FintrackLabelSmallText(text = "سود کل", color = glassColors.text3)
                            FintrackLabelMediumText(text = totalInterest.toPersianPrice() + " " + stringResource(Res.string.currency_toman), color = GlassGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    FintrackLabelMediumText(
                        text = "اعمال در فرم",
                        color = GlassGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { onApply() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val glassColors = LocalGlassColors.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FintrackLabelSmallText(text = label, color = glassColors.text3)
        TextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
            singleLine = true,
            colors = glassTextFieldColors(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = glassColors.text,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                FintrackBodyMediumText(text = "۰".toPersianDigits(), color = glassColors.text3)
            }
        )
    }
}

@Composable
fun TitledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp, modifier = modifier, tone = GlassTone.Strong) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FintrackLabelSmallText(text = label, color = glassColors.text3)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = glassColors.text,
                    fontWeight = FontWeight.SemiBold
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
                    keyboardType = keyboardType
                ),
                cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            FintrackBodyMediumText(text = placeholder, color = glassColors.text3)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun PickerValue(
    label: String,
    color: Color,
    icon: Any? = null
) {
    val glassColors = LocalGlassColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = glassColors.text
        )
        when (icon) {
            is org.jetbrains.compose.resources.DrawableResource -> {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(16.dp)
                )
            }

            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(16.dp)
                )
            }

            else -> {
                Icon(
                    painter = painterResource(Res.drawable.ic_1),
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
