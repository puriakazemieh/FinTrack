package com.tosantechno.filter

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.DatePickerField
import com.kazemieh.designsystem.component.FilterButton
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText


@Composable
fun ReportTopBar(
    onTransactionTypeSelected: Int,
    onTransactionTypeClicked: (Int) -> Unit,

    isAllSourceSelected: Boolean = true,
    selectedSources: Set<Pair<Int, String>> = emptySet(),
    onSourceClicked: () -> Unit,

    isAllCategorySelected: Boolean = true,
    selectedCategories: Set<Pair<Int, String>>,
    onCategoryClicked: () -> Unit,

    isAllTAgSelected: Boolean = true,
    selectedTags: Set<Pair<Int, String>> = emptySet(),
    onTagClicked: () -> Unit,

    isAllPersonSelected: Boolean = true,
    selectedPersons: Set<Pair<Int, String>> = emptySet(),
    onPersonClicked: () -> Unit,

    isShowArrowButton: Boolean = true,
    onDateClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    textDate: String
) {


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
        ) {

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TransactionType.entries.forEachIndexed { index, option ->
                        val text = when (option) {
                            TransactionType.INCOME -> {
                                stringResource(R.string.incoming)
                            }

                            TransactionType.EXPENSE -> {
                                stringResource(R.string.outcoming)
                            }

                            TransactionType.TRANSFER -> {
                                stringResource(R.string.transfer)
                            }

                            else -> {
                                stringResource(R.string.all)
                            }

                        }
                        SegmentedButton(
                            selected = onTransactionTypeSelected == option.count,
                            onClick = { onTransactionTypeClicked(option.count) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TransactionType.entries.size
                            ),
                        ) {
                            FintrackBodyMediumText(text = text)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                val labelTextSource =
                    if (isAllSourceSelected) stringResource(R.string.all_source)
                    else if (selectedSources.isEmpty()) stringResource(R.string.empty_source)
                    else stringResource(R.string.sources, selectedSources.size)

                Selector(
                    selected = selectedSources,
                    isAllSelected = isAllSourceSelected,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    labelText = labelTextSource,
                    onClick = onSourceClicked,
                )

                val labelTextCategory =
                    if (isAllCategorySelected) stringResource(R.string.all_category)
                    else if (selectedCategories.isEmpty()) stringResource(R.string.empty_category)
                    else stringResource(R.string.categories, selectedCategories.size)

                Selector(
                    selected = selectedCategories,
                    isAllSelected = isAllCategorySelected,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    labelText = labelTextCategory,
                    onClick = onCategoryClicked
                )

            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                val labelTextSource =
                    if (isAllTAgSelected) stringResource(R.string.all_tags)
                    else if (selectedTags.isEmpty()) stringResource(R.string.empty_tag)
                    else stringResource(R.string.tags, selectedTags.size)

                Selector(
                    selected = selectedTags,
                    isAllSelected = isAllTAgSelected,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    labelText = labelTextSource,
                    onClick = onTagClicked,
                )

                val labelTextCategory =
                    if (isAllPersonSelected) stringResource(R.string.all_person)
                    else if (selectedPersons.isEmpty()) stringResource(R.string.empty_person)
                    else stringResource(R.string.persons, selectedPersons.size)

                Selector(
                    selected = selectedPersons,
                    isAllSelected = isAllPersonSelected,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    labelText = labelTextCategory,
                    onClick = onPersonClicked,
                )

            }

            DatePeriodSelector(
                dateText = textDate,
                isShowArrowButton = isShowArrowButton,
                onPrevClick = onPrevClick,
                onNextClick = onNextClick,
                onDateClick = onDateClick
            )


        }
    }
}

@Composable
private fun Selector(
    selected: Set<Pair<Int, String>> = emptySet(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    labelText: String,
    isAllSelected: Boolean = false,
    onClick: () -> Unit
) {
    val sourceTextStyle =
        if (selected.isEmpty()) MaterialTheme.typography.bodyMedium
        else MaterialTheme.typography.labelSmall
    FintrackOutlinedTextField(
        value = if (isAllSelected) "" else if (selected.size == 1) selected.first().second
        else selected.joinToString(", ") { it.second },
        onClick = onClick,
        readOnly = true,
        enabled = false,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        singleLine = true,
        textStyle = sourceTextStyle,
        modifier = modifier,
        label = {
            FintrackBodyMediumText(text = labelText)
        }

    )
}

@Composable
fun DatePeriodSelector(
    dateText: String,
    isShowArrowButton: Boolean = true,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isShowArrowButton) {
            IconButton(
                modifier = Modifier.weight(0.1f),
                onClick = onPrevClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }

        FilterButton(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 4.dp)
                .weight(0.6f),
            text = dateText,
            onClick = onDateClick
        )

        if (isShowArrowButton) {
            IconButton(
                modifier = Modifier.weight(0.1f),
                onClick = onNextClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterBottomSheet(
    onDismiss: () -> Unit,
    onDateRange: (DateFilterType) -> Unit,
    onToggleCustomDateSheet: () -> Unit,
    startDate: String? = null,
    endDate: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        val dayFilters = DateFilterType.entries.filter {
            it in listOf(
                DateFilterType.TODAY,
                DateFilterType.YESTERDAY,
                DateFilterType.TOMORROW
            )
        }

        val weekFilters = DateFilterType.entries.filter {
            it in listOf(
                DateFilterType.THIS_WEEK,
                DateFilterType.LAST_WEEK,
                DateFilterType.NEXT_WEEK
            )
        }

        val monthFilters = DateFilterType.entries.filter {
            it in listOf(
                DateFilterType.THIS_MONTH,
                DateFilterType.LAST_MONTH,
                DateFilterType.NEXT_MONTH
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            LazyColumn {
                item {
                    FilterRow(
                        filters = dayFilters,
                        onToggleCustomDateSheet = onToggleCustomDateSheet,
                        onDateRange = onDateRange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterRow(
                        filters = weekFilters,
                        onToggleCustomDateSheet = onToggleCustomDateSheet,
                        onDateRange = onDateRange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterRow(
                        filters = monthFilters,
                        onToggleCustomDateSheet = onToggleCustomDateSheet,
                        onDateRange = onDateRange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilterRow(
                        filters = listOf(DateFilterType.CUSTOM_RANGE),
                        onToggleCustomDateSheet = onToggleCustomDateSheet,
                        onDateRange = onDateRange,
                        isCustomDate = true,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    filters: List<DateFilterType>,
    onToggleCustomDateSheet: () -> Unit,
    onDateRange: (DateFilterType) -> Unit,
    isCustomDate: Boolean = false,
    startDate: String? = null,
    endDate: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (filter == DateFilterType.CUSTOM_RANGE) onToggleCustomDateSheet()
                        else onDateRange(filter)

                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                val text =
                    if (isCustomDate && startDate != null && endDate != null) "$startDate - $endDate"
                    else stringResource(filter.titleResId)

                FintrackBodyMediumText(
                    modifier = Modifier.padding(16.dp),
                    text = text
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateBottomSheet(
    onDismiss: () -> Unit,
    start: Pair<String?, Long?>? = null,
    end: Pair<String?, Long?>? = null,
    isError: Boolean = false,
    onSubmit: (Pair<String?, Long?>?, Pair<String?, Long?>?) -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var startDate by remember { mutableStateOf(start) }
    var endDate by remember { mutableStateOf(end) }
    var startDateTimeStamp by remember { mutableStateOf(startDate?.second) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                FintrackTitleMediumText(stringResource(R.string.custom_date))
                Spacer(Modifier.height(8.dp))

                DatePickerField(
                    selectedDate = startDate?.first ?: stringResource(R.string.not_choose),
                    labelText = stringResource(R.string.start),
                    isError = isError && startDate == null,
                    onDateSelected = { date, timeStamp ->
                        startDate = date to timeStamp
                        startDateTimeStamp = timeStamp
                    }
                )

                Spacer(Modifier.height(8.dp))

                DatePickerField(
                    selectedDate = endDate?.first ?: stringResource(R.string.not_choose),
                    labelText = stringResource(R.string.end),
                    isError = isError && endDate == null,
                    disableBeforeDate = startDateTimeStamp,
                    clickable = startDate != null,
                    onDateSelected = { date, timeStamp ->
                        endDate = date to timeStamp
                    }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSubmit(startDate, endDate) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(
                        text = stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }

    }
}

