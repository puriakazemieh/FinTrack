package com.kazemieh.designsystem.component.jalali


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.component.glass.SheetFrame
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_select_date
import fintrack.core.designsystem.generated.resources.dp_cancel
import fintrack.core.designsystem.generated.resources.dp_confirm
import fintrack.core.designsystem.generated.resources.dp_dow_fri
import fintrack.core.designsystem.generated.resources.dp_dow_mon
import fintrack.core.designsystem.generated.resources.dp_dow_sat
import fintrack.core.designsystem.generated.resources.dp_dow_sun
import fintrack.core.designsystem.generated.resources.dp_dow_thu
import fintrack.core.designsystem.generated.resources.dp_dow_tue
import fintrack.core.designsystem.generated.resources.dp_dow_wed
import fintrack.core.designsystem.generated.resources.dp_select_month
import fintrack.core.designsystem.generated.resources.dp_select_year
import fintrack.core.designsystem.generated.resources.dp_today
import fintrack.core.designsystem.generated.resources.month_aban
import fintrack.core.designsystem.generated.resources.month_azar
import fintrack.core.designsystem.generated.resources.month_bahman
import fintrack.core.designsystem.generated.resources.month_dey
import fintrack.core.designsystem.generated.resources.month_esfand
import fintrack.core.designsystem.generated.resources.month_farvardin
import fintrack.core.designsystem.generated.resources.month_khordad
import fintrack.core.designsystem.generated.resources.month_mehr
import fintrack.core.designsystem.generated.resources.month_mordad
import fintrack.core.designsystem.generated.resources.month_ordibehesht
import fintrack.core.designsystem.generated.resources.month_shahrivar
import fintrack.core.designsystem.generated.resources.month_tir
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalaliDatePickerBottomSheet(
    openSheet: MutableState<Boolean>,
    initialDate: JalaliCalendar? = null,
    disableBeforeDate: JalaliCalendar? = null,
    disableAfterDate: JalaliCalendar? = null,
    onConfirm: (JalaliCalendar) -> Unit,
) {
    if (openSheet.value) {
        val initial = remember(initialDate) { initialDate ?: JalaliCalendar() }
        var tempSelectedDate by remember { mutableStateOf(initial) }

        SheetFrame(
            title = stringResource(Res.string.action_select_date),
            onDismiss = { openSheet.value = false },
            isFullScreen = false
        ) {
            JalaliCalendarView(
                openDialog = openSheet,
                initialDate = initial,
                disableBeforeDate = disableBeforeDate,
                disableAfterDate = disableAfterDate,
                onSelectDay = { tempSelectedDate = it },
                onConfirm = { onConfirm(tempSelectedDate) },
                backgroundColor = Color.Transparent,
                dayOfWeekLabelColor = com.kazemieh.designsystem.GlassText3,
                dropDownColor = com.kazemieh.designsystem.GlassText,
                selectedIconColor = com.kazemieh.designsystem.GlassGreen,
                textColorHighlight = com.kazemieh.designsystem.GlassGreen,
                textColor = com.kazemieh.designsystem.GlassText,
                textDisabledColor = com.kazemieh.designsystem.GlassText3.copy(alpha = 0.38f),
                cancelBtnColor = com.kazemieh.designsystem.GlassRed,
                confirmBtnColor = com.kazemieh.designsystem.GlassGreen,
                todayBtnColor = com.kazemieh.designsystem.GlassGreen,
                todayBtnVisible = true,
                nextPreviousBtnColor = com.kazemieh.designsystem.GlassText
            )
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
    todayBtnVisible: Boolean = false
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
        val startOffset = JalaliCalendar(jalali.year, jalali.month, 1).dayOfWeek

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
                    text = jalali.year.toPersianDigits(),
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
                        Res.string.dp_dow_sat, Res.string.dp_dow_sun, Res.string.dp_dow_mon,
                        Res.string.dp_dow_tue, Res.string.dp_dow_wed, Res.string.dp_dow_thu,
                        Res.string.dp_dow_fri
                    ).forEach { labelRes ->
                        FintrackLabelSmallText(
                            text = stringResource(labelRes),
                            color = dayOfWeekLabelColor
                        )
                    }
                }

                var currentDay = 1 - startOffset
                while (currentDay <= jalali.monthLength) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        Arrangement.SpaceAround
                    ) {
                        repeat(7) { index ->
                            val day = currentDay + index
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
                                        text = day.toPersianDigits(),
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
                        }
                    }
                    currentDay += 7
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
                    listOf(
                        4 to stringResource(Res.string.month_tir),
                        3 to stringResource(Res.string.month_khordad),
                        2 to stringResource(Res.string.month_ordibehesht),
                        1 to stringResource(Res.string.month_farvardin)
                    ),
                    listOf(
                        8 to stringResource(Res.string.month_aban),
                        7 to stringResource(Res.string.month_mehr),
                        6 to stringResource(Res.string.month_shahrivar),
                        5 to stringResource(Res.string.month_mordad)
                    ),
                    listOf(
                        12 to stringResource(Res.string.month_esfand),
                        11 to stringResource(Res.string.month_bahman),
                        10 to stringResource(Res.string.month_dey),
                        9 to stringResource(Res.string.month_azar)
                    )
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
                                text = index.toPersianDigits(),
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        enabled = selectedDate != null,
                        onClick = {
                            onConfirm(selectedDate!!)
                            openDialog.value = false
                        }
                    ) {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.dp_confirm),
                            color = if (selectedDate != null) confirmBtnColor else textDisabledColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onClick = { openDialog.value = false }) {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.dp_cancel),
                            color = cancelBtnColor
                        )
                    }
                }

                if (todayBtnVisible || (selectedDate != today || jalali.year != today.year || jalali.month != today.month)) {
                    TextButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
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
}
