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
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.label_font_app
import fintrack.core.designsystem.generated.resources.label_font_sahel
import fintrack.core.designsystem.generated.resources.label_font_shabnam
import fintrack.core.designsystem.generated.resources.label_font_vazirmatn
import fintrack.core.designsystem.generated.resources.label_text_size
import fintrack.core.designsystem.generated.resources.text_scale_extra_large
import fintrack.core.designsystem.generated.resources.text_scale_extra_small
import fintrack.core.designsystem.generated.resources.text_scale_large
import fintrack.core.designsystem.generated.resources.text_scale_medium
import fintrack.core.designsystem.generated.resources.text_scale_small
import fintrack.core.designsystem.generated.resources.title_text_settings
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextSettingsBottomSheet(
    currentScale: TextScale,
    currentFont: TextFont,
    onScaleChanged: (TextScale) -> Unit,
    onFontChanged: (TextFont) -> Unit,
    onDismiss: () -> Unit
) {
    SheetFrame(
        title = stringResource(Res.string.title_text_settings),
        onDismiss = onDismiss,
        primaryButtonText = stringResource(Res.string.confirm),
        onPrimaryClick = onDismiss,
        isFullScreen = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            FintrackBodyMediumText(text = stringResource(Res.string.label_text_size))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextScale.entries.forEach { scale ->
                    val isSelected = scale == currentScale
                    val label = when (scale) {
                        TextScale.EXTRA_SMALL -> stringResource(Res.string.text_scale_extra_small)
                        TextScale.SMALL -> stringResource(Res.string.text_scale_small)
                        TextScale.MEDIUM -> stringResource(Res.string.text_scale_medium)
                        TextScale.LARGE -> stringResource(Res.string.text_scale_large)
                        TextScale.EXTRA_LARGE -> stringResource(Res.string.text_scale_extra_large)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onScaleChanged(scale) },
                        label = { FintrackBodyMediumText(label) }
                    )
                }
            }
            
            HorizontalDivider()
            
            FintrackBodyMediumText(text = stringResource(Res.string.label_font_app))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextFont.entries.forEach { font ->
                    val isSelected = font == currentFont
                    val label = when (font) {
                        TextFont.VAZIRMATN -> stringResource(Res.string.label_font_vazirmatn)
                        TextFont.SHABNAM -> stringResource(Res.string.label_font_shabnam)
                        TextFont.SAHEL -> stringResource(Res.string.label_font_sahel)
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
