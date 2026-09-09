package com.kazemieh.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppLanguage
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.SheetFrame
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_english
import fintrack.core.designsystem.generated.resources.label_german
import fintrack.core.designsystem.generated.resources.label_persian
import fintrack.core.designsystem.generated.resources.title_language_settings
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsBottomSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetFrame(
        title = stringResource(Res.string.title_language_settings),
        onDismiss = onDismiss,
        isFullScreen = false,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { language ->
                val label = when (language) {
                    AppLanguage.PERSIAN -> stringResource(Res.string.label_persian)
                    AppLanguage.ENGLISH -> stringResource(Res.string.label_english)
                    AppLanguage.GERMAN -> stringResource(Res.string.label_german)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(language) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = language == selectedLanguage,
                        onClick = { onLanguageSelected(language) },
                    )
                    Spacer(Modifier.width(8.dp))
                    FintrackBodyMediumText(text = label)
                }
            }
        }
    }
}
