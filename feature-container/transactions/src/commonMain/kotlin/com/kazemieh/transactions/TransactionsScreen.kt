package com.kazemieh.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategorySelectionBottomSheet
import com.kazemieh.common.DateFilterType
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourceSelectionBottomSheet
import com.kazemieh.person.ui.list.PersonSelectionBottomSheet
import com.kazemieh.tag.ui.list.TagSelectionBottomSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.report.TransactionListByFilterScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: TransactionsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    val colors = FinTrackPickerColors.rainbow()

    val activeFilters = remember(
        state.selectedCategories,
        state.selectedSources,
        state.selectedTag,
        state.selectedPerson
    ) {
        buildList {
            state.selectedCategories.forEach { category ->
                add(
                    FilterChipData(
                        id = "cat_${category.id}",
                        label = category.name,
                        color = colors.firstOrNull { it.id == category.colorId }?.color,
                        type = FilterType.Category
                    )
                )
            }
            state.selectedSources.forEach { source ->
                add(
                    FilterChipData(
                        id = "src_${source.id}",
                        label = source.name,
                        type = FilterType.Source
                    )
                )
            }
            state.selectedTag.forEach { tag ->
                add(
                    FilterChipData(
                        id = "tag_${tag.id}",
                        label = tag.name,
                        color = colors.firstOrNull { it.id == tag.colorId }?.color,
                        type = FilterType.Tag
                    )
                )
            }
            state.selectedPerson.forEach { person ->
                add(
                    FilterChipData(
                        id = "pers_${person.id}",
                        label = person.name,
                        type = FilterType.Person
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassBg0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large)
        ) {
            TxHeader(
                isSearchActive = state.isSearchActive,
                isFilterActive = activeFilters.isNotEmpty(),
                onSearchClick = { viewModel.onIntent(ReportIntent.OnToggleSearch) },
                onFilterClick = { /* Show general filter if needed, or toggle specific ones */ }
            )

            PeriodSelector(
                currentPeriod = state.dateFilterType,
                periodLabel = state.textDate.asString(),
                periodSubLabel = "نمایش بر اساس ${state.dateFilterType.name}", // Placeholder or helper
                onPeriodSelected = { viewModel.onIntent(ReportIntent.OnDateRange(it)) },
                onPrevClick = { viewModel.onIntent(ReportIntent.OnPrevClick) },
                onNextClick = { viewModel.onIntent(ReportIntent.OnNextClick) }
            )

            ActiveFilters(
                filters = activeFilters,
                onRemove = { filter ->
                    when (filter.type) {
                        FilterType.Category -> {
                            val newSet =
                                state.selectedCategories.filter { "cat_${it.id}" != filter.id }
                                    .toSet()
                            viewModel.onIntent(ReportIntent.OnCategoriesSelected(newSet, false))
                        }

                        FilterType.Source -> {
                            val newSet =
                                state.selectedSources.filter { "src_${it.id}" != filter.id }.toSet()
                            viewModel.onIntent(ReportIntent.OnSourcesSelected(newSet, false))
                        }

                        FilterType.Tag -> {
                            val newSet =
                                state.selectedTag.filter { "tag_${it.id}" != filter.id }.toSet()
                            viewModel.onIntent(ReportIntent.OnTagSelected(newSet, false))
                        }

                        FilterType.Person -> {
                            val newSet =
                                state.selectedPerson.filter { "pers_${it.id}" != filter.id }.toSet()
                            viewModel.onIntent(ReportIntent.OnPersonSelected(newSet, false))
                        }
                    }
                },
                onClearAll = {
                    viewModel.onIntent(ReportIntent.OnCategoriesSelected(emptySet(), false))
                    viewModel.onIntent(ReportIntent.OnSourcesSelected(emptySet(), false))
                    viewModel.onIntent(ReportIntent.OnTagSelected(emptySet(), false))
                    viewModel.onIntent(ReportIntent.OnPersonSelected(emptySet(), false))
                }
            )

            TransactionListByFilterScreen(
                selectedSources = state.selectedSources,
                isAllSources = state.isAllSourceSelected,
                selectedCategories = state.selectedCategories,
                isAllCategories = state.isAllCategorySelected,
                selectedTags = state.selectedTag,
                isAllTags = state.isAllTAgSelected,
                selectedPersons = state.selectedPerson,
                isAllPersons = state.isAllPersonSelected,
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

        if (state.isCategorySheetVisible) {
            CategorySelectionBottomSheet(
                initialSelectionPairs = state.selectedCategories,
                isAllSelected = state.isAllCategorySelected,
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
                isAllSelected = state.isAllTAgSelected,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(ReportIntent.OnTagSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(ReportIntent.OnToggleTagSheet) }
            )
        }

        if (state.isPersonSheetVisible) {
            PersonSelectionBottomSheet(
                initialSelectionPairs = state.selectedPerson,
                isAllSelected = state.isAllPersonSelected,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(ReportIntent.OnPersonSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(ReportIntent.OnTogglePersonSheet) }
            )
        }

        if (state.isSourceSheetVisible) {
            SourceSelectionBottomSheet(
                initialSelectionPairs = state.selectedSources,
                isAllSelected = state.isAllSourceSelected,
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
                onSubmit = { startPair, endPair ->
                    viewModel.onIntent(
                        ReportIntent.OnDateSheetSubmit(
                            startDate = startPair?.first,
                            startTimeStamp = startPair?.second,
                            endDate = endPair?.first,
                            endTimeStamp = endPair?.second
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

