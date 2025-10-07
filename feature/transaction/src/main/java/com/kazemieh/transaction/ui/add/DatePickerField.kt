package com.kazemieh.transaction.ui.add

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.gmail.hamedvakhide.compose_jalali_datepicker.JalaliDatePickerDialog
import com.kazemieh.designsystem.FintrackFontFamily
import com.kazemieh.designsystem.FintrackTypography
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.transaction.R
import ir.huri.jcal.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String, Long) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }
    JalaliDatePickerDialog(
        openDialog = openDialog,
        fontFamily = FintrackFontFamily,
        fontSize = FintrackTypography.titleMedium.fontSize,
        initialDate = JalaliCalendar(),
        onSelectDay = {},
        onConfirm = {
            val gregorian = it.toGregorian()
            val timestamp = gregorian.timeInMillis
//            val date = "${it.day} ${it.year} ${it.monthString}"
            val date = "${it.day}/${it.monthString}/${it.year}"
            onDateSelected(date, timestamp)
        },
        backgroundColor = MaterialTheme.colorScheme.surface,
        textColor = MaterialTheme.colorScheme.onBackground,
        selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
        textColorHighlight = MaterialTheme.colorScheme.primary,
        dropDownColor = MaterialTheme.colorScheme.primary,
        todayBtnColor = MaterialTheme.colorScheme.primary,
        cancelBtnColor = MaterialTheme.colorScheme.primary,
        confirmBtnColor = MaterialTheme.colorScheme.primary,
        dayOfWeekLabelColor = MaterialTheme.colorScheme.primary,
        nextPreviousBtnColor = MaterialTheme.colorScheme.primary
    )


    FintrackOutlinedTextField(
        value = selectedDate,
        onClick = { openDialog.value = true },
        readOnly = true,
        enabled = false,
        label = { FintrackBodyMediumText(text = stringResource(R.string.date)) }
    )


}
