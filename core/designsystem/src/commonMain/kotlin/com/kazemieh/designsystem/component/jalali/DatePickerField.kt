package com.kazemieh.designsystem.component.jalali

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.kazemieh.common.toPersianDigits
import com.kazemieh.jalali.JalaliCalendar
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.glass.SheetFrame
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_select_date
import fintrack.core.designsystem.generated.resources.date
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: String? = "${JalaliCalendar().day.toPersianDigits()} / ${JalaliCalendar().monthString} / ${JalaliCalendar().year.toPersianDigits()}",
    labelText: String = stringResource(Res.string.date),
    isError: Boolean = false,
    clickable: Boolean = true,
    disableBeforeDate: Long? = null,
    onDateSelected: (String, Long) -> Unit
) {
    val openSheet = remember { mutableStateOf(false) }

    JalaliDatePickerBottomSheet(
        openSheet = openSheet,
        disableBeforeDate = if (disableBeforeDate != null)
            JalaliCalendar.fromTimestamp(disableBeforeDate) else null,
        onConfirm = {
            val timestamp = it.toTimestamp()
            val date =
                "${it.day.toPersianDigits()} / ${it.monthString} / ${it.year.toPersianDigits()}"
            onDateSelected(date, timestamp)
        },
    )

    FintrackOutlinedTextField(
        value = selectedDate
            ?: "${JalaliCalendar().day.toPersianDigits()} / ${JalaliCalendar().monthString} / ${JalaliCalendar().year.toPersianDigits()}",
        onClick = { if (clickable) openSheet.value = true },
        readOnly = true,
        enabled = false,
        isError = isError,
        label = { FintrackBodyMediumText(text = labelText) }
    )
}


@Composable
fun JalaliDatePickerDialog(
    openDialog: MutableState<Boolean>,
    onConfirm: (JalaliCalendar) -> Unit,
) {
    val space = LocalSpacing.current
    if (openDialog.value) {
        Dialog(
            onDismissRequest = { openDialog.value = false },
//            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        openDialog.value = false
                    }
                    .padding(horizontal = space.large)
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                    color = GlassColor
                ) {
                    JalaliCalendarView(
                        openDialog = openDialog,
                        initialDate = null,
                        disableBeforeDate = null,
                        disableAfterDate = null,
                        onSelectDay = { },
                        onConfirm = {
                            onConfirm(it)
                        },
                        backgroundColor = Color.Transparent,
                        dayOfWeekLabelColor = GlassText3,
                        dropDownColor = GlassText,
                        selectedIconColor = GlassGreen,
                        textColorHighlight = GlassGreen,
                        textColor = GlassText,
                        textDisabledColor = GlassText3.copy(alpha = 0.38f),
                        cancelBtnColor = GlassRed,
                        confirmBtnColor = GlassGreen,
                        todayBtnColor = GlassGreen,
                        nextPreviousBtnColor = GlassText
                    )
                }
            }

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalaliDatePickerBottomSheet(
    openSheet: MutableState<Boolean>,
    disableBeforeDate: JalaliCalendar? = null,
    onConfirm: (JalaliCalendar) -> Unit,
) {
    if (openSheet.value) {
        SheetFrame(
            title = stringResource(Res.string.action_select_date),
            onDismiss = { openSheet.value = false },
            isFullScreen = false
        ) {
            JalaliCalendarView(
                openDialog = openSheet,
                initialDate = null,
                disableBeforeDate = disableBeforeDate,
                disableAfterDate = null,
                onSelectDay = { },
                onConfirm = {
                    onConfirm(it)
                },
                backgroundColor = Color.Transparent,
                dayOfWeekLabelColor = GlassText3,
                dropDownColor = GlassText,
                selectedIconColor = GlassGreen,
                textColorHighlight = GlassGreen,
                textColor = GlassText,
                textDisabledColor = GlassText3.copy(alpha = 0.38f),
                cancelBtnColor = GlassRed,
                confirmBtnColor = GlassGreen,
                todayBtnColor = GlassGreen,
                nextPreviousBtnColor = GlassText,
                todayBtnVisible = true
            )
        }
    }
}
