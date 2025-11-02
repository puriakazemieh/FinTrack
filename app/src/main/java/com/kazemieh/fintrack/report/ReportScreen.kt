package com.kazemieh.fintrack.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.CategoryListSelectionBottomSheet
import com.kazemieh.common.DateFilterType
import com.kazemieh.designsystem.component.DatePickerField
import com.kazemieh.designsystem.component.FilterButton
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.financialsource.ui.SourceListSelectionBottomSheet
import com.kazemieh.fintrack.R
import com.kazemieh.transaction.ui.report.TransactionFilterType
import com.kazemieh.transaction.ui.report.TransactionListByFilterScreen
import org.koin.androidx.compose.koinViewModel


@Composable
fun ReportScreen(
    viewModel: ReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column {

            Spacer(modifier = Modifier.height(8.dp))

            ReportTopBar(onIntent = viewModel::onIntent, state = state)

            TransactionListByFilterScreen(
                selectedSources = state.selectedSources,
                selectedCategories = state.selectedCategories,
                selectedTransactionType = state.selectedTransactionType.count,
                fromTimestamp = state.timeStampRangeStart,
                toTimestamp = state.timeStampRangeEnd
            )

        }


        if (state.isSourceSheetVisible) {
            SourceListSelectionBottomSheet(
                onSourceClick = {
                    viewModel.onIntent(ReportFilterIntent.OnSourcesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportFilterIntent.OnToggleSourceSheet)
                }
            )
        }

        if (state.isCategorySheetVisible) {
            CategoryListSelectionBottomSheet(
                selectedTransactionType = state.selectedTransactionType.count,
                onCategoryClick = {
                    viewModel.onIntent(ReportFilterIntent.OnCategoriesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportFilterIntent.OnToggleCategorySheet)
                }
            )
        }

        if (state.isDateSheetVisible) {
            DateFilterSheet(state, viewModel::onIntent)
        }

        if (state.isCustomDateSheetVisible) {
            CustomRangeSheet(
                state = state,
                onIntent = viewModel::onIntent
            )
        }

    }
}

@Composable
fun ReportTopBar(onIntent: (ReportFilterIntent) -> Unit, state: ReportFilterState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TransactionTypeFilter.entries.forEachIndexed { index, option ->
                        val text = when (option.count) {
                            1 -> {
                                stringResource(com.kazemieh.transaction.R.string.incoming)
                            }

                            2 -> {
                                stringResource(com.kazemieh.transaction.R.string.outcoming)
                            }

                            else -> {
                                stringResource(com.kazemieh.transaction.R.string.all)
                            }
                        }
                        SegmentedButton(
                            selected = state.selectedTransactionType == option,
                            onClick = {
                                onIntent(ReportFilterIntent.OnTransactionTypeSelected(option))
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TransactionFilterType.entries.size
                            ),
                        ) {

                            FintrackBodyMediumText(text = text)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                val sourceTextStyle =
                    if (state.selectedSources.isEmpty()) MaterialTheme.typography.bodyMedium
                    else MaterialTheme.typography.labelSmall
                FintrackOutlinedTextField(
                    value = if (state.selectedSources.size == 1) state.selectedSources.first().second
                    else state.selectedSources.joinToString(", ") { it.second },
                    onClick = { onIntent(ReportFilterIntent.OnToggleSourceSheet) },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    textStyle = sourceTextStyle,
                    modifier = Modifier.weight(1f),
                    label = {
                        val text =
                            if (state.selectedSources.isEmpty()) stringResource(R.string.all_source)
                            else stringResource(R.string.sources, state.selectedSources.size)

                        FintrackBodyMediumText(text = text)

                    }

                )

                val categoryTextStyle =
                    if (state.selectedCategories.isEmpty()) MaterialTheme.typography.bodyMedium
                    else MaterialTheme.typography.labelSmall
                FintrackOutlinedTextField(
                    value = if (state.selectedCategories.size == 1) state.selectedCategories.first().second
                    else state.selectedCategories.joinToString(", ") { it.second },
                    onClick = { onIntent(ReportFilterIntent.OnToggleCategorySheet) },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    textStyle = categoryTextStyle,
                    modifier = Modifier.weight(1f),
                    label = {
                        val text =
                            if (state.selectedCategories.isEmpty()) stringResource(R.string.all_category)
                            else stringResource(R.string.categories, state.selectedCategories.size)

                        FintrackBodyMediumText(text = text)

                    }

                )
            }

            DatePeriodSelector(
                state = state,
                onIntent = onIntent
            )


        }
    }
}


@Composable
fun DatePeriodSelector(
    state: ReportFilterState,
    onIntent: (ReportFilterIntent) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.customRangeStart == null && state.customRangeEnd == null) {
            IconButton(
                modifier = Modifier.weight(0.1f),
                onClick = {
                    onIntent(ReportFilterIntent.OnShiftFilter(1))
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "قبلی")
            }
        }

        val text = if (state.customRangeStart != null && state.customRangeEnd != null)
            "${state.customRangeStart} - ${state.customRangeEnd}"
        else
            state.selectedDateFilter.title

        FilterButton(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 4.dp)
                .weight(0.8f),
            text = text,
            onClick = { onIntent(ReportFilterIntent.OnToggleDateSheet) }
        )

        if (state.customRangeStart == null && state.customRangeEnd == null) {
            IconButton(
                modifier = Modifier.weight(0.1f),
                onClick = {
                    onIntent(ReportFilterIntent.OnShiftFilter(-1))
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بعدی")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterSheet(
    state: ReportFilterState,
    onIntent: (ReportFilterIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onIntent(ReportFilterIntent.OnToggleDateSheet) },
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
                    FilterRow(dayFilters, state, onIntent)
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterRow(weekFilters, state, onIntent)
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterRow(monthFilters, state, onIntent)
                    Spacer(modifier = Modifier.height(12.dp))
                    FilterRow(listOf(DateFilterType.CUSTOM_RANGE), state, onIntent)
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    filters: List<DateFilterType>,
    state: ReportFilterState,
    onIntent: (ReportFilterIntent) -> Unit
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
                        if (filter == DateFilterType.CUSTOM_RANGE)
                            onIntent(ReportFilterIntent.OnToggleCustomDateSheet)
                        else
                            onIntent(ReportFilterIntent.OnDateFilterSelected(filter))
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                val text = if (filter == DateFilterType.CUSTOM_RANGE) {
                    if (state.customRangeStart != null && state.customRangeEnd != null)
                        "${state.customRangeStart} - ${state.customRangeEnd}"
                    else filter.title
                } else filter.title

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
fun CustomRangeSheet(
    state: ReportFilterState,
    onIntent: (ReportFilterIntent) -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onIntent(ReportFilterIntent.OnToggleCustomDateSheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                FintrackTitleMediumText("بازه انتخابی")
                Spacer(Modifier.height(8.dp))

                DatePickerField(
                    selectedDate = state.customRangeStart ?: "انتخاب نشده",
                    labelText = "شروع",
                    onDateSelected = { date, timeStamp ->
                        onIntent(
                            ReportFilterIntent.OnCustomRangeStartSelected(
                                date = date,
                                timeStamp = timeStamp
                            )
                        )
                    }
                )

                Spacer(Modifier.height(8.dp))

                DatePickerField(
                    selectedDate = state.customRangeEnd ?: "انتخاب نشده",
                    labelText = "پایان",
                    onDateSelected = { date, timeStamp ->
                        onIntent(
                            ReportFilterIntent.OnCustomRangeEndSelected(
                                date = date,
                                timeStamp = timeStamp
                            )
                        )
                    }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onIntent(ReportFilterIntent.OnDateSheetSubmit) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(
                        text = stringResource(com.kazemieh.category.R.string.confirm),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }

    }
}
