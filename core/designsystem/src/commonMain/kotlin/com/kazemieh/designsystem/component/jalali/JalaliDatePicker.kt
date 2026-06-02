package com.kazemieh.designsystem.component.jalali


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kazemieh.designsystem.component.*
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Opens a Jalali DatePicker dialog with the given content.
 *
 * Example usage:
 *
 * @param openDialog Dialog will be visible as long as openDialog value is true.
 * @param disableBeforeDate Days before this date are disabled and can't be selected.
 * @param disableAfterDate Days after this date are disabled and can't be selected.
 * @param initialDate Specify a date to be shown when dialog opens.
 * @param onSelectDay Called when a day is selected.
 * @param onConfirm Called when confirm button is clicked.
 * @param backgroundColor Background color of the dialog.
 * @param textColor Color of the text.
 * @param selectedIconColor Color of selected day (month, year) circular icon.
 * @param textColorHighlight Color of current day (month, year) text.
 * @param dropDownColor Color of the year and month drop-down text.
 * @param dayOfWeekLabelColor Color for the day of the week label text.
 * @param confirmBtnColor Color of confirm button.
 * @param cancelBtnColor Color of cancel button.
 * @param todayBtnColor Color of today button.
 * @param nextPreviousBtnColor Color of next and previous month button.
 * @param fontFamily changing font for all the text.
 * @param fontSize changing font size for all the text.
 *
 */

@Composable
fun JalaliDatePickerDialog(
    openDialog: MutableState<Boolean>,
    initialDate: JalaliCalendar? = null,
    disableBeforeDate: JalaliCalendar? = null,
    disableAfterDate: JalaliCalendar? = null,
    onSelectDay: (JalaliCalendar) -> Unit,
    onConfirm: (JalaliCalendar) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textDisabledColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    selectedIconColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColorHighlight: Color = MaterialTheme.colorScheme.primary,
    dropDownColor: Color = MaterialTheme.colorScheme.onSurface,
    dayOfWeekLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    confirmBtnColor: Color = MaterialTheme.colorScheme.primary,
    cancelBtnColor: Color = MaterialTheme.colorScheme.error,
    todayBtnColor: Color = MaterialTheme.colorScheme.primary,
    nextPreviousBtnColor: Color = MaterialTheme.colorScheme.onSurface,
    fontFamily: FontFamily = FontFamily.Default,
    fontSize: TextUnit = 14.sp
) {
    if (openDialog.value) {
        Dialog(
            onDismissRequest = { openDialog.value = false },
//            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        // same action as in onDismissRequest
                        openDialog.value = false
                    }
            ) {
                // content
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation,

                    ) {
                    JalaliCalendarView(
                        openDialog = openDialog,
                        initialDate = initialDate,
                        disableBeforeDate = disableBeforeDate,
                        disableAfterDate = disableAfterDate,
                        onSelectDay = {
                            onSelectDay(it)
//                            Log.d("DAGDAG", "onSelect: ${it.day} ${it.monthString} ${it.year}")
                        },
                        onConfirm = {
                            onConfirm(it)
//                            Log.d("DAGDAG", "onConfirm: ${it.day} ${it.monthString} ${it.year}")
                        },
                        backgroundColor = backgroundColor,
                        dayOfWeekLabelColor = dayOfWeekLabelColor,
                        dropDownColor = dropDownColor,
                        selectedIconColor = selectedIconColor,
                        textColorHighlight = textColorHighlight,
                        textColor = textColor,
                        textDisabledColor = textDisabledColor,
                        cancelBtnColor = cancelBtnColor,
                        confirmBtnColor = confirmBtnColor,
                        todayBtnColor = todayBtnColor,
                        nextPreviousBtnColor = nextPreviousBtnColor,
                        fontFamily = fontFamily,
                        fontSize = fontSize
                    )
                }
            }

        }

    }
}


@Composable
fun JalaliCalendarView(
    initialDate: JalaliCalendar?,
    disableBeforeDate: JalaliCalendar?,
    disableAfterDate: JalaliCalendar?,
    openDialog: MutableState<Boolean>,
    onSelectDay: (JalaliCalendar) -> Unit,
    onConfirm: (JalaliCalendar) -> Unit,
    backgroundColor: Color,
    textColor: Color,
    textDisabledColor: Color,
    selectedIconColor: Color,
    textColorHighlight: Color,
    dropDownColor: Color,
    dayOfWeekLabelColor: Color,
    confirmBtnColor: Color,
    cancelBtnColor: Color,
    todayBtnColor: Color,
    nextPreviousBtnColor: Color,
    fontFamily: FontFamily,
    fontSize: TextUnit
) {
    var iconSize by remember { mutableStateOf(43.dp) }
    var weekDaysLabelPadding by remember { mutableStateOf(18.dp) }
    var yearSelectorHeight by remember { mutableStateOf(280.dp) }

    var jalali by remember { mutableStateOf(initialDate ?: JalaliCalendar()) }
    val today = JalaliCalendar()
    var selectedDate by remember { mutableStateOf(initialDate) }
    var pickerType: PickerType by remember { mutableStateOf(PickerType.Day) }

    val windowInfo = LocalWindowInfo.current
    val isLandscape = windowInfo.containerSize.run { width > height }

    SideEffect {
        iconSize = if (isLandscape) 32.dp else 43.dp
        weekDaysLabelPadding = if (isLandscape) 9.dp else 18.dp
        yearSelectorHeight = if (isLandscape) 230.dp else 280.dp
    }

    Column(
        Modifier.background(backgroundColor).animateContentSize()
    ) {
        val firstJomeh = run {
            val dow = JalaliCalendar(jalali.year, jalali.month, 1).dayOfWeek
            if (dow == 7) 7 else 7 - dow
        }

        // header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pickerType == PickerType.Day) {
                val nextMonth =
                    if (jalali.month != 12) JalaliCalendar(jalali.year, jalali.month + 1, 1)
                    else JalaliCalendar(jalali.year + 1, 1, 1)
                val lastCurrent = JalaliCalendar(jalali.year, jalali.month, jalali.monthLength)
                val disabled =
                    disableAfterDate != null && lastCurrent.isAfterOrEqual(disableAfterDate)
                IconButton(
                    onClick = { jalali = nextMonth },
                    enabled = !disabled,
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        Icons.Outlined.KeyboardArrowLeft,
                        null,
                        tint = if (disabled) textDisabledColor else nextPreviousBtnColor
                    )
                }
            } else {
                FilledIconButton(
                    onClick = {}, Modifier.size(iconSize).alpha(0f),
                    colors = IconButtonDefaults.filledIconButtonColors(Color.Transparent)
                ) {
                FintrackLabelSmallText("")
                }
            }

            TextButton(onClick = {
                pickerType = if (pickerType != PickerType.Year) PickerType.Year else PickerType.Day
            }) {
                FintrackTitleMediumText(
                    text = FormatHelper.toPersianNumber(jalali.year.toString()),
                    color = dropDownColor
                )
                Icon(Icons.Outlined.ArrowDropDown, null, tint = dropDownColor)
            }

            TextButton(onClick = {
                pickerType =
                    if (pickerType != PickerType.Month) PickerType.Month else PickerType.Day
            }) {
                FintrackTitleMediumText(
                    text = jalali.monthString,
                    color = dropDownColor
                )
                Icon(Icons.Outlined.ArrowDropDown, null, tint = dropDownColor)
            }

            if (pickerType == PickerType.Day) {
                val prevMonth =
                    if (jalali.month != 1) JalaliCalendar(jalali.year, jalali.month - 1, 1)
                    else JalaliCalendar(jalali.year - 1, 12, 1)
                val firstCurrent = JalaliCalendar(jalali.year, jalali.month, 1)
                val disabled =
                    disableBeforeDate != null && firstCurrent.isBeforeOrEqual(disableBeforeDate)
                IconButton(
                    onClick = { jalali = prevMonth },
                    enabled = !disabled,
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        Icons.Outlined.KeyboardArrowRight,
                        null,
                        tint = if (disabled) textDisabledColor else nextPreviousBtnColor
                    )
                }
            } else {
                FilledIconButton(
                    onClick = {}, Modifier.size(iconSize).alpha(0f),
                    colors = IconButtonDefaults.filledIconButtonColors(Color.Transparent)
                ) {
                FintrackLabelSmallText("")
                }
            }
        }

        when (pickerType) {
            PickerType.Day -> {
                // days of week labels
                Row(
                    Modifier.fillMaxWidth().padding(vertical = weekDaysLabelPadding),
                    Arrangement.SpaceAround
                ) {
                    listOf(
                        Res.string.dp_dow_fri, Res.string.dp_dow_thu, Res.string.dp_dow_wed,
                        Res.string.dp_dow_tue, Res.string.dp_dow_mon, Res.string.dp_dow_sun,
                        Res.string.dp_dow_sat
                    ).forEach { labelRes ->
                        FintrackLabelSmallText(
                            text = stringResource(labelRes),
                            color = dayOfWeekLabelColor
                        )
                    }
                }

                var jomeh = firstJomeh
                while (true) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        Arrangement.SpaceAround
                    ) {
                        var day = jomeh
                        repeat(7) {
                            if (day in 1..jalali.monthLength) {
                                val current = JalaliCalendar(jalali.year, jalali.month, day)
                                val disabled =
                                    (disableBeforeDate != null && current.isBeforeOrEqual(
                                        disableBeforeDate
                                    )) ||
                                            (disableAfterDate != null && current.isAfterOrEqual(
                                                disableAfterDate
                                            ))
                                val selected = selectedDate != null && day == selectedDate!!.day &&
                                        jalali.year == selectedDate!!.year && jalali.month == selectedDate!!.month
                                val isToday =
                                    day == today.day && jalali.year == today.year && jalali.month == today.month

                                FilledIconButton(
                                    onClick = {
                                        selectedDate = current
                                        onSelectDay(selectedDate!!)
                                    },
                                    Modifier.size(iconSize),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (selected) selectedIconColor else Color.Transparent
                                    ),
                                    enabled = !disabled
                                ) {
                                    FintrackBodyMediumText(
                                        text = FormatHelper.toPersianNumber(day.toString()),
                                        color = when {
                                            isToday -> textColorHighlight
                                            disabled -> textDisabledColor
                                            else -> textColor
                                        }
                                    )
                                }
                            } else {
                                FilledIconButton(
                                    onClick = {},
                                    Modifier.size(iconSize).alpha(0f),
                                    colors = IconButtonDefaults.filledIconButtonColors(Color.Transparent)
                                ) {
                                    FintrackBodyMediumText("")
                                }
                            }
                            day--
                        }
                    }
                    if (jomeh >= jalali.monthLength) break
                    jomeh += 7
                }
            }

            PickerType.Month -> {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), Arrangement.Center) {
                    FintrackTitleMediumText(
                        text = stringResource(Res.string.dp_select_month),
                        color = textColor
                    )
                }

                val monthRows = listOf(
                    listOf(4 to "تیر", 3 to "خرداد", 2 to "اردیبهشت", 1 to "فروردین"),
                    listOf(8 to "آبان", 7 to "مهر", 6 to "شهریور", 5 to "مرداد"),
                    listOf(12 to "اسفند", 11 to "بهمن", 10 to "دی", 9 to "آذر")
                )

                for (row in monthRows) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        Arrangement.SpaceAround
                    ) {
                        for ((monthNum, monthName) in row) {
                            val monthDate = JalaliCalendar(jalali.year, monthNum, 1)
                            val disabled = (disableBeforeDate != null && monthDate.isBeforeOrEqual(
                                disableBeforeDate
                            )) ||
                                    (disableAfterDate != null && monthDate.isAfterOrEqual(
                                        disableAfterDate
                                    ))
                            TextButton(
                                onClick = {
                                    jalali = monthDate
                                    pickerType = PickerType.Day
                                },
                                enabled = !disabled
                            ) {
                                FintrackBodyMediumText(
                                    text = monthName,
                                    color = when {
                                        disabled -> textDisabledColor
                                        jalali.month == monthNum -> textColorHighlight
                                        else -> textColor
                                    }
                                )
                            }
                        }
                    }
                }
            }

            PickerType.Year -> {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), Arrangement.Center) {
                    FintrackTitleMediumText(
                        text = stringResource(Res.string.dp_select_year),
                        Modifier.padding(vertical = 8.dp),
                        color = textColor
                    )
                }
                val scrollState =
                    rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, jalali.year - 2))
                LazyColumn(
                    Modifier.fillMaxWidth().height(yearSelectorHeight).padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = scrollState
                ) {
                    items(3000) { index ->
                        Divider()
                        val disabled =
                            (disableBeforeDate != null && index < disableBeforeDate.year) ||
                                    (disableAfterDate != null && index > disableAfterDate.year)
                        Box(
                            Modifier.fillMaxWidth()
                                .clickable(enabled = !disabled) {
                                    var temp = JalaliCalendar(index, jalali.month, 1)
                                    if (disableBeforeDate != null && index <= disableBeforeDate.year && jalali.month <= disableBeforeDate.month) {
                                        temp = JalaliCalendar(
                                            index,
                                            disableBeforeDate.getTomorrow().month,
                                            1
                                        )
                                    } else if (disableAfterDate != null && index >= disableAfterDate.year && jalali.month >= disableAfterDate.month) {
                                        temp = JalaliCalendar(
                                            index,
                                            disableAfterDate.getYesterday().month,
                                            1
                                        )
                                    }
                                    jalali = temp
                                    pickerType = PickerType.Day
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FintrackTitleLargeText(
                                text = FormatHelper.toPersianNumber(index.toString()),
                                color = when {
                                    disabled -> textDisabledColor
                                    jalali.year == index -> textColorHighlight
                                    else -> textColor
                                }
                            )
                        }
                    }
                }
            }
        }

        if (pickerType == PickerType.Day) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    TextButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        enabled = selectedDate != null,
                        onClick = {
                            onConfirm(selectedDate!!)
                            openDialog.value = false
                        }
                    ) {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.dp_confirm),
                            color = if (selectedDate != null) confirmBtnColor else textDisabledColor
                        )
                    }
                    TextButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = { openDialog.value = false }) {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.dp_cancel),
                            color = cancelBtnColor
                        )
                    }
                }
                TextButton(
                    modifier = Modifier.padding(horizontal = 8.dp).alpha(
                        if (selectedDate != today || jalali.year != today.year || jalali.month != today.month) 1f else 0f
                    ),
                    onClick = {
                        val tempToday = JalaliCalendar()
                        jalali = JalaliCalendar(tempToday.year, tempToday.month, 1)
                        selectedDate = JalaliCalendar()
                        onSelectDay(selectedDate!!)
                    }
                ) {
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.dp_today),
                        color = todayBtnColor
                    )
                }
            }
        }
    }
}


@Composable
fun monthTextColorFun(
    currentMonth: Int,
    jalali: JalaliCalendar,
    isDisabled: Boolean,
    textColorHighlight: Color,
    textColor: Color,
    textDisabledColor: Color
): Color {
    return if (isDisabled)
        textDisabledColor
    else if (jalali.month == currentMonth) {
        textColorHighlight
    } else {
        textColor
    }
}

@Composable
fun yearTextColorFun(
    currentYear: Int,
    yearIndex: Int,
    isDisabled: Boolean,
    textColorHighlight: Color,
    textColor: Color,
    textDisabledColor: Color

): Color {
    return if (isDisabled)
        textDisabledColor
    else if (currentYear == yearIndex) {
        textColorHighlight
    } else {
        textColor
    }
}