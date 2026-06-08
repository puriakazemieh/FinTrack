package com.kazemieh.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassRangeSlider
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.financialsource.ui.list.SourceFilterSelectionContent
import com.kazemieh.person.ui.list.PersonFilterSelectionContent
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.custom_range
import fintrack.core.designsystem.generated.resources.date
import fintrack.core.designsystem.generated.resources.label_amount_with_unit
import fintrack.core.designsystem.generated.resources.label_custom_range_selected
import fintrack.core.designsystem.generated.resources.label_this_year
import fintrack.core.designsystem.generated.resources.label_type
import fintrack.core.designsystem.generated.resources.msg_filters_combined
import fintrack.core.designsystem.generated.resources.report
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.type_expense
import fintrack.core.designsystem.generated.resources.type_income
import fintrack.core.designsystem.generated.resources.type_transfer
import org.jetbrains.compose.resources.stringResource

import androidx.compose.material3.TextButton
import fintrack.core.designsystem.generated.resources.btn_clear_all

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterBottomSheet(
    state: TransactionsState,
    snackbarHostState: SnackbarHostState,
    onIntent: (TransactionsIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetFrame(
        title = stringResource(Res.string.report),
        sub = stringResource(Res.string.msg_filters_combined),
        onDismiss = onDismiss,
        trailingContent = {
            TextButton(onClick = { onIntent(TransactionsIntent.ResetFilters) }) {
                FintrackLabelMediumText(
                    text = stringResource(Res.string.btn_clear_all),
                    color = GlassRed
                )
            }
        }
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Section
            FilterSection(title = stringResource(Res.string.date)) {
                val periods = listOf(
                    DateFilterType.TODAY to stringResource(Res.string.today),
                    DateFilterType.THIS_WEEK to stringResource(Res.string.this_week),
                    DateFilterType.THIS_MONTH to stringResource(Res.string.this_month),
                    DateFilterType.THIS_YEAR to stringResource(Res.string.label_this_year)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(periods) { (type, label) ->
                            val active = state.dateFilterType == type
                            Chip(
                                active = active,
                                color = GlassGreen,
                                onClick = { onIntent(TransactionsIntent.OnDateRange(type)) }
                            ) {
                                FintrackLabelMediumText(
                                    text = label,
                                    color = if (active) Color.White else GlassText2
                                )
                            }
                        }
                        item {
                            Chip(
                                active = state.dateFilterType == DateFilterType.CUSTOM_RANGE,
                                color = GlassGreen,
                                onClick = { onIntent(TransactionsIntent.OnToggleCustomDateSheet) }
                            ) {
                                FintrackLabelMediumText(
                                    text = stringResource(Res.string.custom_range),
                                    color = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE) Color.White else GlassText2
                                )
                            }
                        }
                    }

                    if (state.textPeriodRange.asString()
                            .isNotEmpty() || state.dateFilterType == DateFilterType.CUSTOM_RANGE
                    ) {
                        val label = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE) {
                            stringResource(
                                Res.string.label_custom_range_selected,
                                state.textDate.asString()
                            )
                        } else {
                            state.textPeriodRange.asString()
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            padding = 10.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FintrackLabelSmallText(
                                    text = label,
                                    color = GlassText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Type Section
            FilterSection(title = stringResource(Res.string.label_type)) {
                val types = listOf(
                    TransactionType.ALL to stringResource(Res.string.all),
                    TransactionType.EXPENSE to stringResource(Res.string.type_expense),
                    TransactionType.INCOME to stringResource(Res.string.type_income),
                    TransactionType.TRANSFER to stringResource(Res.string.type_transfer)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { (type, label) ->
                        val active = state.selectedTransactionType == type
                        val color = when (type) {
                            TransactionType.INCOME -> GlassGreen
                            TransactionType.EXPENSE -> GlassRed
                            TransactionType.TRANSFER -> GlassBlue
                            else -> GlassText
                        }
                        Chip(
                            active = active,
                            color = color,
                            onClick = { onIntent(TransactionsIntent.OnTransactionTypeSelected(type)) }
                        ) {
                            FintrackLabelMediumText(
                                text = label,
                                color = if (active) Color.White else GlassText2
                            )
                        }
                    }
                }
            }

            // Amount Range Section
            GlassRangeSlider(
                title = "مبلغ",
                value = state.selectedMinAmount..state.selectedMaxAmount,
                onValueChange = { range ->
                    onIntent(TransactionsIntent.OnAmountRangeChanged(range.start, range.endInclusive))
                },
                valueRange = state.minAmountLimit..state.maxAmountLimit
            )

            // Sources Section
            SourceFilterSelectionContent(
                selectedSources = state.selectedSources,
                isAllSelected = state.isAllSourceSelected,
                snackbarHostState = snackbarHostState,
                onSelectionChanged = { sources, isAll ->
                    onIntent(TransactionsIntent.OnSourcesSelected(sources, isAll))
                }
            )

            // Categories Section
            CategoryFilterSelectionContent(
                selectedCategories = state.selectedCategories,
                selectedTransactionType = state.selectedTransactionType,
                isAllSelected = state.isAllCategorySelected,
                snackbarHostState = snackbarHostState,
                onSelectionChanged = { categories, isAll ->
                    onIntent(TransactionsIntent.OnCategoriesSelected(categories, isAll))
                }
            )

            // Persons Section
            PersonFilterSelectionContent(
                selectedPersons = state.selectedPerson,
                isAllSelected = state.isAllPersonSelected,
                snackbarHostState = snackbarHostState,
                onSelectionChanged = { persons, isAll ->
                    onIntent(TransactionsIntent.OnPersonSelected(persons, isAll))
                }
            )

            // Tags Section
            TagFilterSelectionContent(
                selectedTags = state.selectedTag,
                isAllSelected = state.isAllTAgSelected,
                snackbarHostState = snackbarHostState,
                onSelectionChanged = { tags, isAll ->
                    onIntent(TransactionsIntent.OnTagSelected(tags, isAll))
                }
            )
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(
            text = title,
            color = GlassText2,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

