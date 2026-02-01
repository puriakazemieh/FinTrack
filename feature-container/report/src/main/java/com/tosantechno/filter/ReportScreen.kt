package com.tosantechno.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategorySelectionBottomSheet
import com.kazemieh.common.DateFilterType
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.financialsource.ui.list.SourceSelectionBottomSheet
import com.kazemieh.person.ui.list.PersonSelectionBottomSheet
import com.kazemieh.tag.ui.list.TagSelectionBottomSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.report.TransactionListByFilterScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReportScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            Spacer(modifier = Modifier.height(space.mediumSmall))

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
                isAllCategorySelected = state.isAllCategorySelected,
                onCategoryClicked = { viewModel.onIntent(ReportIntent.OnToggleCategorySheet) },

                selectedSources = state.selectedSources,
                isAllSourceSelected = state.isAllSourceSelected,
                onSourceClicked = { viewModel.onIntent(ReportIntent.OnToggleSourceSheet) },

                selectedTags = state.selectedTag,
                isAllTAgSelected = state.isAllTAgSelected,
                onTagClicked = { viewModel.onIntent(ReportIntent.OnToggleTagSheet) },

                selectedPersons = state.selectedPerson,
                isAllPersonSelected = state.isAllPersonSelected,
                onPersonClicked = { viewModel.onIntent(ReportIntent.OnTogglePersonSheet) },

                isShowArrowButton = state.isShowArrowButton,
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
                enableAnimationChart = state.enableAnimationChart,
                onEdit = { transactionWithRelations ->
                    viewModel.onIntent(
                        ReportIntent.ShowTransactionBottomSheet(transactionWithRelations)
                    )
                },
                onDelete = { transactionWithRelations ->
                    viewModel.onIntent(
                        ReportIntent.DeleteTransactionBottomSheet(transactionWithRelations)
                    )
                },
            )
        }

        // ✅ اسم‌های جدید selection bottom sheet ها
        if (state.isCategorySheetVisible) {
            CategorySelectionBottomSheet(
                initialSelectionPairs = state.selectedCategories,
                selectedTransactionType = state.selectedTransactionType,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(ReportIntent.OnCategoriesSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(ReportIntent.OnToggleCategorySheet) }
            )
        }

        if (state.isTagSheetVisible) {
            TagSelectionBottomSheet(
                initialSelectionPairs = state.selectedTag,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(ReportIntent.OnTagSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(ReportIntent.OnToggleTagSheet) }
            )
        }

        if (state.isPersonSheetVisible) {
            PersonSelectionBottomSheet(
                initialSelectionPairs = state.selectedPerson,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(ReportIntent.OnPersonSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(ReportIntent.OnTogglePersonSheet) }
            )
        }

        if (state.isSourceSheetVisible) {
            SourceSelectionBottomSheet(
                initialSelectionPairs = state.selectedSources,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(ReportIntent.OnSourcesSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(ReportIntent.OnToggleSourceSheet) }
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
                start = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE)
                    state.startDate to state.startDateTimeStamp
                else null,
                end = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE)
                    state.endDate to state.endDateTimeStamp
                else null,
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

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                snackbarHostState = snackbarHostState,
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(ReportIntent.ShowTransactionBottomSheet()) },
                transactionAdded = { viewModel.onIntent(ReportIntent.ShowTransactionBottomSheet()) },
            )
        }

        if (state.showDeleteTransaction) {
            DeleteTransactionBottomSheet(
                snackbarHostState = snackbarHostState,
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(ReportIntent.DeleteTransactionBottomSheet()) },
                transactionDeleted = { viewModel.onIntent(ReportIntent.DeleteTransactionBottomSheet()) },
            )
        }
    }
}

