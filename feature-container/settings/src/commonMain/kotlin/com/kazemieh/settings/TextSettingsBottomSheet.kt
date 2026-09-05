package com.kazemieh.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.TextScale
import com.kazemieh.designsystem.TextFont
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.SheetFrame

@OptIn(ExperimentalMaterial3Api::class)
:Composable
fun TextSettingsBottomSheet(
    currentScale: TextScale,
    currentFont: TextFont,
    onScaleChanged: (TextScale) -> Unit,
    onFontChanged: (TextFont) -> Unit,
    onDismiss: () -> Unit
) {
    SheetFrame(
        title = "تنػیمات متن",
        onDismiss = onDismiss,
        primaryButtonText = "تایید",
        onPrimaryClick = onDismiss,
        isFullScreen = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            FintrackBodyMediumText(text = "اندازه متق:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextScale.entries.forEach { scale ->
                    val isSelected = scale == currentScale
                    val label = when (scale) {
                        TextScale.EXTRA_SMALL -> "XS"
                        TextScale.SMALL -> "S"
                        TextScale.MEDIUM -> "M"
                        TextScale.LARGE -> "L"
                        TextScale.EXTRA_LARGE -> "XL"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onScaleChanged(scale) },
                        label = { FintrackBodyMediumText(label) }
                    )
                }
            }
            
            HorizontalDivider()
            
            FintrackBodyMediumText(text = "فونت برنامه:")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextFont.entries.forEach { font ->
                    val isSelected = font == currentFont
                    val label = when (font) {
                        TextFont.VAZIRMATN -> "وزیرمتۆ (Vazirmatn)"
                        TextFont.SHABNAM -> "شبنم (Shabnam)"
                        TextFont.SAHEL -> "ساحل (Sahel)"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFontChanged(font) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onFontChanged(font) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FintrackBodyMediumText(text = label)
                    }
                }
            }
        }
    }
}
