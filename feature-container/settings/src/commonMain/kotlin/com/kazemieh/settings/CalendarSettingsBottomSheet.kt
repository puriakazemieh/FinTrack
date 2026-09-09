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
import com.kazemieh.designsystem.CalendarSystem
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.SheetFrame
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_calendar_fa
import fintrack.core.designsystem.generated.resources.label_gregorian
import fintrack.core.designsystem.generated.resources.label_jalali
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettingsBottomSheet(
    selectedCalendarSystem: CalendarSystem,
    onCalendarSelected: (CalendarSystem) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetFrame(
        title = stringResource(Res.string.label_calendar_fa),
        onDismiss = onDismiss,
        isFullScreen = false,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CalendarSystem.entries.forEach { calendarSystem ->
                val label = when (calendarSystem) {
                    CalendarSystem.JALALI -> stringResource(Res.string.label_jalali)
                    CalendarSystem.GREGORIAN -> stringResource(Res.string.label_gregorian)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onCalendarSelected(calendarSystem) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = calendarSystem == selectedCalendarSystem,
                        onClick = { onCalendarSelected(calendarSystem) },
                    )
                    Spacer(Modifier.width(8.dp))
                    FintrackBodyMediumText(text = label)
                }
            }
        }
    }
}
