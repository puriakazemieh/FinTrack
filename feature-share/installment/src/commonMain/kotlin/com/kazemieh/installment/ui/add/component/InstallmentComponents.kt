package com.kazemieh.installment.ui.add.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.ic_1
import org.jetbrains.compose.resources.painterResource

@Composable
fun LargeInstallmentField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        padding = 16.dp
    ) {
        FintrackOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { FintrackBodyMediumText(text = label) },
            textColor = GlassText,
            containerColor = Color.Transparent,
            unfocusedBorderColor = GlassEdge,
            focusedBorderColor = GlassGreen,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PickerValue(
    label: String,
    color: Color,
    icon: Any? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = GlassText
        )
        when (icon) {
            is org.jetbrains.compose.resources.DrawableResource -> {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = GlassText3,
                    modifier = Modifier.size(16.dp)
                )
            }

            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlassText3,
                    modifier = Modifier.size(16.dp)
                )
            }

            else -> {
                Icon(
                    painter = painterResource(Res.drawable.ic_1),
                    contentDescription = null,
                    tint = GlassText3,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
