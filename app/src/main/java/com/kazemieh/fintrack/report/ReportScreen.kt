package com.kazemieh.fintrack.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.CategoryListSelectionBottomSheet
import com.kazemieh.common.DateFilterType
import com.kazemieh.financialsource.ui.SourceListSelectionBottomSheet
import com.kazemieh.person.ui.PersonListSelectionBottomSheet
import com.kazemieh.tag.ui.TagListSelectionBottomSheet
import com.kazemieh.transaction.ui.report.TransactionListByFilterScreen
import com.tosantechno.filter.CustomDateBottomSheet
import com.tosantechno.filter.DateFilterBottomSheet
import com.tosantechno.filter.ReportTopBar
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
            val label = when (val labelValue = state.textDate) {
                is Int -> stringResource(id = labelValue)
                is String -> labelValue
                else -> ""
            }
            ReportTopBar(
                onTransactionTypeSelected = state.selectedTransactionType,
                onTransactionTypeClicked = {
                    viewModel.onIntent(ReportIntent.OnTransactionTypeSelected(it))
                },
                selectedCategories = state.selectedCategories,
                onCategoryClicked = { viewModel.onIntent(ReportIntent.OnToggleCategorySheet) },
                selectedSources = state.selectedSources,
                onTagClicked = { viewModel.onIntent(ReportIntent.OnToggleTagSheet) },
                selectedTags = state.selectedTag,
                onPersonClicked = { viewModel.onIntent(ReportIntent.OnTogglePersonSheet) },
                selectedPersons = state.selectedPerson,
                isShowArrowButton = state.isShowArrowButton,
                onSourceClicked = { viewModel.onIntent(ReportIntent.OnToggleSourceSheet) },
                onDateClick = { viewModel.onIntent(ReportIntent.OnToggleDateSheet) },
                onNextClick = { viewModel.onIntent(ReportIntent.OnNextClick) },
                onPrevClick = { viewModel.onIntent(ReportIntent.OnPrevClick) },
                textDate = label

            )

            TransactionListByFilterScreen(
                selectedSources = state.selectedSources,
                selectedCategories = state.selectedCategories,
                selectedTags = state.selectedTag,
                selectedPersons = state.selectedPerson,
                selectedTransactionType = state.selectedTransactionType,
                fromTimestamp = state.startDateTimeStamp,
                toTimestamp = state.endDateTimeStamp,
                enableAnimationChart = state.enableAnimationChart
            )

        }


        if (state.isTagSheetVisible) {
            TagListSelectionBottomSheet(
                onTagClick = {
                    viewModel.onIntent(ReportIntent.OnTagSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportIntent.OnToggleTagSheet)
                }
            )
        }

        if (state.isPersonSheetVisible) {
            PersonListSelectionBottomSheet(
                onPersonClick = {
                    viewModel.onIntent(ReportIntent.OnPersonSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportIntent.OnTogglePersonSheet)
                }
            )
        }

        if (state.isSourceSheetVisible) {
            SourceListSelectionBottomSheet(
                onSourceClick = {
                    viewModel.onIntent(ReportIntent.OnSourcesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportIntent.OnToggleSourceSheet)
                }
            )
        }

        if (state.isCategorySheetVisible) {
            CategoryListSelectionBottomSheet(
                selectedTransactionType = state.selectedTransactionType,
                onCategoryClick = {
                    viewModel.onIntent(ReportIntent.OnCategoriesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportIntent.OnToggleCategorySheet)
                }
            )
        }

        if (state.isDateSheetVisible) {
            DateFilterBottomSheet(
                onDismiss = { viewModel.onIntent(ReportIntent.OnToggleDateSheet) },
                onToggleCustomDateSheet = { viewModel.onIntent(ReportIntent.OnToggleCustomDateSheet) },
                onDateRange = { viewModel.onIntent(ReportIntent.OnDateRange(it)) },
                startDate = state.startDate,
                endDate = state.endDate,
            )
        }

        if (state.isCustomDateSheetVisible) {
            CustomDateBottomSheet(
                onDismiss = { viewModel.onIntent(ReportIntent.OnToggleCustomDateSheet) },
                start = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE) state.startDate to state.startDateTimeStamp else null,
                end = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE) state.endDate to state.endDateTimeStamp else null,
                isError = state.isError,
                onSubmit = { startDate, endDate ->
                    viewModel.onIntent(
                        ReportIntent.OnDateSheetSubmit(
                            startDate = startDate?.first,
                            startTimeStamp = startDate?.second,
                            endDate = endDate?.first,
                            endTimeStamp = endDate?.second
                        )
                    )
                }
            )
        }

    }
}

