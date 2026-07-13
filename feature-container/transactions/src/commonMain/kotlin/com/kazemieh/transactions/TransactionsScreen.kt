package com.kazemieh.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategorySelectionBottomSheet
import com.kazemieh.designsystem.component.glass.PeriodNavigator
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourceSelectionBottomSheet
import com.kazemieh.person.ui.list.PersonSelectionBottomSheet
import com.kazemieh.tag.ui.list.TagSelectionBottomSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.report.TransactionListByFilterScreen
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.income
import fintrack.core.designsystem.generated.resources.label_to
import fintrack.core.designsystem.generated.resources.outcoming
import fintrack.core.designsystem.generated.resources.placeholder_period_range
import fintrack.core.designsystem.generated.resources.transfer
import fintrack.core.designsystem.generated.resources.unit_toman_short
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsScreen(
    resetFilters: Boolean = true,
    categoryId: Long? = null,
    sourceId: Long? = null,
    tagId: Long? = null,
    personId: Long? = null,
    transactionType: TransactionType? = null,
    query: String? = null,
    onNavigateToSearch: () -> Unit = {},
    viewModel: TransactionsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        resetFilters,
        state.resetFiltersHandled,
        categoryId,
        sourceId,
        tagId,
        personId,
        transactionType,
        query
    ) {
        if (resetFilters && !state.resetFiltersHandled) {
            viewModel.onIntent(TransactionsIntent.ResetFilters)
        }

        if (categoryId != null || sourceId != null || tagId != null || personId != null || transactionType != null || query != null) {
            viewModel.onIntent(
                TransactionsIntent.ApplyFilter(
                    categoryId = categoryId,
                    sourceId = sourceId,
                    tagId = tagId,
                    personId = personId,
                    transactionType = transactionType,
                    query = query
                )
            )
        }
    }

    val space = LocalSpacing.current

    val colors = FinTrackPickerColors.rainbow()
    val tomanUnit = stringResource(Res.string.unit_toman_short)
    val labelTo = stringResource(Res.string.label_to)

    val incomeLabel = stringResource(Res.string.income)
    val expenseLabel = stringResource(Res.string.outcoming)
    val transferLabel = stringResource(Res.string.transfer)

    val activeFilters = remember(
        state.selectedTransactionType,
        state.selectedCategories,
        state.selectedSources,
        state.selectedTag,
        state.selectedPerson,
        state.selectedMinAmount,
        state.selectedMaxAmount,
        state.minAmountLimit,
        state.maxAmountLimit,
        tomanUnit,
        incomeLabel,
        expenseLabel,
        transferLabel
    ) {
        buildList {
            if (state.selectedTransactionType != TransactionType.ALL) {
                val label = when (state.selectedTransactionType) {
                    TransactionType.INCOME -> incomeLabel
                    TransactionType.EXPENSE -> expenseLabel
                    TransactionType.TRANSFER -> transferLabel
                    else -> ""
                }
                val color = when (state.selectedTransactionType) {
                    TransactionType.INCOME -> GlassGreen
                    TransactionType.EXPENSE -> GlassRed
                    TransactionType.TRANSFER -> GlassBlue
                    else -> null
                }
                add(
                    FilterChipData(
                        id = "tx_type",
                        label = label,
                        color = color,
                        type = FilterType.TransactionType
                    )
                )
            }
            if (state.selectedMinAmount > state.minAmountLimit || state.selectedMaxAmount < state.maxAmountLimit) {
                val label = "${
                    state.selectedMinAmount.toLong().toSignedPersianPrice()
                } $labelTo ${state.selectedMaxAmount.toLong().toSignedPersianPrice()} $tomanUnit"
                add(
                    FilterChipData(
                        id = "amount_range",
                        label = label,
                        color = GlassGreen,
                        type = FilterType.Amount
                    )
                )
            }
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

    FintrackScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large)
        ) {
            TxHeader(
                isSearchActive = state.isSearchActive,
                isFilterActive = activeFilters.isNotEmpty(),
                onSearchClick = onNavigateToSearch,
                onFilterClick = { viewModel.onIntent(TransactionsIntent.OnToggleFilterSheet) }
            )

            PeriodNavigator(
                currentPeriod = state.dateFilterType,
                periodLabel = state.textDate.asString(),
                periodSubLabel = state.textPeriodRange.asString().ifEmpty {
                    if (state.dateFilterType == DateFilterType.TODAY) "" else stringResource(Res.string.placeholder_period_range)
                },
                onPeriodSelected = { type ->
                    if (type == DateFilterType.CUSTOM_RANGE) {
                        viewModel.onIntent(TransactionsIntent.OnToggleCustomDateSheet)
                    } else {
                        viewModel.onIntent(TransactionsIntent.OnDateRange(type))
                    }
                },
                onPrevClick = { viewModel.onIntent(TransactionsIntent.OnPrevClick) },
                onNextClick = { viewModel.onIntent(TransactionsIntent.OnNextClick) }
            )

            ActiveFilters(
                filters = activeFilters,
                onRemove = { filter ->
                    when (filter.type) {
                        FilterType.Category -> {
                            val newSet =
                                state.selectedCategories.filter { "cat_${it.id}" != filter.id }
                                    .toSet()
                            viewModel.onIntent(
                                TransactionsIntent.OnCategoriesSelected(
                                    newSet,
                                    newSet.isEmpty()
                                )
                            )
                        }

                        FilterType.Source -> {
                            val newSet =
                                state.selectedSources.filter { "src_${it.id}" != filter.id }.toSet()
                            viewModel.onIntent(
                                TransactionsIntent.OnSourcesSelected(
                                    newSet,
                                    newSet.isEmpty()
                                )
                            )
                        }

                        FilterType.Tag -> {
                            val newSet =
                                state.selectedTag.filter { "tag_${it.id}" != filter.id }.toSet()
                            viewModel.onIntent(
                                TransactionsIntent.OnTagSelected(
                                    newSet,
                                    newSet.isEmpty()
                                )
                            )
                        }

                        FilterType.Person -> {
                            val newSet =
                                state.selectedPerson.filter { "pers_${it.id}" != filter.id }.toSet()
                            viewModel.onIntent(
                                TransactionsIntent.OnPersonSelected(
                                    newSet,
                                    newSet.isEmpty()
                                )
                            )
                        }

                        FilterType.Amount -> {
                            viewModel.onIntent(
                                TransactionsIntent.OnAmountRangeChanged(
                                    state.minAmountLimit,
                                    state.maxAmountLimit
                                )
                            )
                        }

                        FilterType.TransactionType -> {
                            viewModel.onIntent(
                                TransactionsIntent.OnTransactionTypeSelected(TransactionType.ALL)
                            )
                        }
                    }
                },
                onClearAll = {
                    viewModel.onIntent(TransactionsIntent.OnTransactionTypeSelected(TransactionType.ALL))
                    viewModel.onIntent(TransactionsIntent.OnCategoriesSelected(emptySet(), true))
                    viewModel.onIntent(TransactionsIntent.OnSourcesSelected(emptySet(), true))
                    viewModel.onIntent(TransactionsIntent.OnTagSelected(emptySet(), true))
                    viewModel.onIntent(TransactionsIntent.OnPersonSelected(emptySet(), true))
                    viewModel.onIntent(
                        TransactionsIntent.OnAmountRangeChanged(
                            state.minAmountLimit,
                            state.maxAmountLimit
                        )
                    )
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
                minAmount = state.selectedMinAmount.toLong(),
                maxAmount = state.selectedMaxAmount.toLong(),
                query = state.searchQuery.ifBlank { null },
                onEdit = { transactionWithRelations ->
                    viewModel.onIntent(
                        TransactionsIntent.ShowTransactionBottomSheet(transactionWithRelations)
                    )
                },
                onDelete = { transactionWithRelations ->
                    viewModel.onIntent(
                        TransactionsIntent.DeleteTransactionBottomSheet(transactionWithRelations)
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
                    viewModel.onIntent(TransactionsIntent.OnCategoriesSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnToggleCategorySheet) }
            )
        }

        if (state.isTagSheetVisible) {
            TagSelectionBottomSheet(
                initialSelectionPairs = state.selectedTag,
                isAllSelected = state.isAllTAgSelected,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(TransactionsIntent.OnTagSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnToggleTagSheet) }
            )
        }

        if (state.isPersonSheetVisible) {
            PersonSelectionBottomSheet(
                initialSelectionPairs = state.selectedPerson,
                isAllSelected = state.isAllPersonSelected,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(TransactionsIntent.OnPersonSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnTogglePersonSheet) }
            )
        }

        if (state.isSourceSheetVisible) {
            SourceSelectionBottomSheet(
                initialSelectionPairs = state.selectedSources,
                isAllSelected = state.isAllSourceSelected,
                onConfirmPairs = { pairs, isAll ->
                    viewModel.onIntent(TransactionsIntent.OnSourcesSelected(pairs, isAll))
                },
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnToggleSourceSheet) }
            )
        }

        if (state.isDateSheetVisible) {
            DateFilterBottomSheet(
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnToggleDateSheet) },
                onToggleCustomDateSheet = { viewModel.onIntent(TransactionsIntent.OnToggleCustomDateSheet) },
                onDateRange = { viewModel.onIntent(TransactionsIntent.OnDateRange(it)) },
                startDate = state.startDate,
                endDate = state.endDate,
            )
        }

        if (state.isCustomDateSheetVisible) {
            CustomDateBottomSheet(
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnToggleCustomDateSheet) },
                start = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE)
                    state.startDate to state.startDateTimeStamp
                else null,
                end = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE)
                    state.endDate to state.endDateTimeStamp
                else null,
                isError = state.isError,
                onSubmit = { startPair, endPair ->
                    viewModel.onIntent(
                        TransactionsIntent.OnDateSheetSubmit(
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
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(TransactionsIntent.ShowTransactionBottomSheet()) },
                transactionAdded = { viewModel.onIntent(TransactionsIntent.ShowTransactionBottomSheet()) },
            )
        }

        if (state.showDeleteTransaction) {
            DeleteTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(TransactionsIntent.DeleteTransactionBottomSheet()) },
                transactionDeleted = { viewModel.onIntent(TransactionsIntent.DeleteTransactionBottomSheet()) },
            )
        }

        if (state.isFilterSheetVisible) {
            TransactionFilterBottomSheet(
                state = state,
                onIntent = viewModel::onIntent,
                onDismiss = { viewModel.onIntent(TransactionsIntent.OnToggleFilterSheet) }
            )
        }
    }
}
