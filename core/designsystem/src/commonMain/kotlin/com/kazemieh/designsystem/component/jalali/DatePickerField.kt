package com.kazemieh.designsystem.component.jalali

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.vazirmatnFontFamily
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.date
import kotlinx.coroutines.launch
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        JalaliDatePickerBottomSheet(
            openSheet = openSheet,
            disableBeforeDate = disableBeforeDate,
            onConfirm = {
                val timestamp = it.toTimestamp()
                val date = "${it.day.toPersianDigits()} / ${it.monthString} / ${it.year.toPersianDigits()}"
                onDateSelected(date, timestamp)
            },
        )
    }
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
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        dayOfWeekLabelColor = MaterialTheme.colorScheme.primary,
                        dropDownColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                        textColorHighlight = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onBackground,
                        textDisabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        cancelBtnColor = MaterialTheme.colorScheme.primary,
                        confirmBtnColor = MaterialTheme.colorScheme.primary,
                        todayBtnColor = MaterialTheme.colorScheme.primary,
                        nextPreviousBtnColor = MaterialTheme.colorScheme.primary
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
    disableBeforeDate: Long? = null,
    onConfirm: (JalaliCalendar) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val space = LocalSpacing.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (openSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openSheet.value = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = space.large, vertical = space.mediumLarge)
                    .clip(RoundedCornerShape(space.mediumLarge))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JalaliCalendarView(
                    openDialog = openSheet,
                    initialDate = null,
                    disableBeforeDate = if (disableBeforeDate != null)
                        JalaliCalendar.fromTimestamp(disableBeforeDate) else null,
                    disableAfterDate = null,
                    onSelectDay = { },
                    onConfirm = {
                        onConfirm(it)
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            openSheet.value = false
                        }
                    },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    dayOfWeekLabelColor = MaterialTheme.colorScheme.primary,
                    dropDownColor = MaterialTheme.colorScheme.primary,
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    textColorHighlight = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onBackground,
                    textDisabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    cancelBtnColor = MaterialTheme.colorScheme.primary,
                    confirmBtnColor = MaterialTheme.colorScheme.primary,
                    todayBtnColor = MaterialTheme.colorScheme.primary,
                    nextPreviousBtnColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}