package com.kazemieh.designsystem.component.jalali

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.gregorianMonthLength
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.dp_cancel
import fintrack.core.designsystem.generated.resources.dp_confirm
import fintrack.core.designsystem.generated.resources.dp_dow_fri
import fintrack.core.designsystem.generated.resources.dp_dow_mon
import fintrack.core.designsystem.generated.resources.dp_dow_sat
import fintrack.core.designsystem.generated.resources.dp_dow_sun
import fintrack.core.designsystem.generated.resources.dp_dow_thu
import fintrack.core.designsystem.generated.resources.dp_dow_tue
import fintrack.core.designsystem.generated.resources.dp_dow_wed
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

/** Compact Gregorian counterpart of the Jalali date picker. */
@Composable
internal fun GregorianCalendarView(
    openSheet: MutableState<Boolean>,
    initialDate: JalaliCalendar?,
    disableBeforeDate: JalaliCalendar?,
    disableAfterDate: JalaliCalendar?,
    onConfirm: (JalaliCalendar) -> Unit,
) {
    val glassColors = LocalGlassColors.current
    val timeZone = TimeZone.currentSystemDefault()
    val initialGregorian = remember(initialDate) {
        initialDate?.toGregorian()?.let { LocalDate(it.first, it.second, it.third) }
            ?: Clock.System.now().toLocalDateTime(timeZone).date
    }
    var displayedMonth by remember(initialGregorian) { mutableStateOf(LocalDate(initialGregorian.year, initialGregorian.monthNumber, 1)) }
    var selectedDate by remember(initialGregorian) { mutableStateOf(initialGregorian) }
    val minTimestamp = disableBeforeDate?.toTimestamp(timeZone)
    val maxTimestamp = disableAfterDate?.toTimestamp(timeZone)

    fun isSelectable(date: LocalDate): Boolean {
        val timestamp = date.atStartOfDayIn(timeZone).toEpochMilliseconds()
        return (minTimestamp == null || timestamp > minTimestamp) && (maxTimestamp == null || timestamp < maxTimestamp)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { displayedMonth = displayedMonth.plus(-1, DateTimeUnit.MONTH) }) {
                Icon(Icons.Outlined.KeyboardArrowRight, null, tint = glassColors.text)
            }
            FintrackTitleMediumText(
                text = "${displayedMonth.year} / ${displayedMonth.monthNumber.toString().padStart(2, '0')}",
                color = glassColors.text,
            )
            IconButton(onClick = { displayedMonth = displayedMonth.plus(1, DateTimeUnit.MONTH) }) {
                Icon(Icons.Outlined.KeyboardArrowLeft, null, tint = glassColors.text)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf(
                Res.string.dp_dow_sun, Res.string.dp_dow_mon, Res.string.dp_dow_tue,
                Res.string.dp_dow_wed, Res.string.dp_dow_thu, Res.string.dp_dow_fri,
                Res.string.dp_dow_sat,
            ).forEach { label -> FintrackLabelSmallText(stringResource(label), color = glassColors.text3) }
        }

        val leadingBlanks = displayedMonth.dayOfWeek.isoDayNumber % 7
        var day = 1 - leadingBlanks
        val monthLength = gregorianMonthLength(displayedMonth.year, displayedMonth.monthNumber)
        while (day <= monthLength) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                repeat(7) { index ->
                    val dayNumber = day + index
                    if (dayNumber in 1..monthLength) {
                        val date = LocalDate(displayedMonth.year, displayedMonth.monthNumber, dayNumber)
                        val selected = date == selectedDate
                        FilledIconButton(
                            onClick = { selectedDate = date },
                            enabled = isSelectable(date),
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (selected) GlassGreen else Color.Transparent,
                            ),
                        ) {
                            FintrackBodyMediumText(
                                text = dayNumber.toString(),
                                color = if (selected) Color.White else glassColors.text,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    } else {
                        Box(Modifier.size(40.dp))
                    }
                }
            }
            day += 7
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { openSheet.value = false }) {
                FintrackBodyMediumText(stringResource(Res.string.dp_cancel), color = GlassRed)
            }
            TextButton(
                enabled = isSelectable(selectedDate),
                onClick = {
                    onConfirm(JalaliCalendar.fromGregorian(selectedDate.year, selectedDate.monthNumber, selectedDate.dayOfMonth))
                    openSheet.value = false
                },
            ) {
                FintrackBodyMediumText(stringResource(Res.string.dp_confirm), color = GlassGreen)
            }
        }
    }
}
