package com.kazemieh.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.model.asString
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterBottomSheet(
    state: TransactionsState,
    onIntent: (TransactionsIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetFrame(
        title = stringResource(Res.string.report),
        sub = stringResource(Res.string.msg_filters_combined),
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                                    color = if (active) GlassBg0 else GlassText2
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
                                    color = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE) GlassBg0 else GlassText2
                                )
                            }
                        }
                    }

                    if (state.textPeriodRange.asString().isNotEmpty() || state.dateFilterType == DateFilterType.CUSTOM_RANGE) {
                        val label = if (state.dateFilterType == DateFilterType.CUSTOM_RANGE) {
                            stringResource(Res.string.label_custom_range_selected, state.textDate.asString())
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
                                color = if (active) GlassBg0 else GlassText2
                            )
                        }
                    }
                }
            }

            // Sources Section
            MultiSelectSection(
                title = stringResource(Res.string.financial_sources),
                items = state.selectedSources.toList(),
                isAllSelected = state.isAllSourceSelected,
                onAllClick = { onIntent(TransactionsIntent.OnSourcesSelected(emptySet(), isAllSourceSelected = true)) },
                onAddClick = { onIntent(TransactionsIntent.OnToggleSourceSheet) },
                chipLabel = { it.name },
                chipColor = { GlassBlue }
            )

            // Categories Section
            MultiSelectSection(
                title = stringResource(Res.string.category),
                items = state.selectedCategories.toList(),
                isAllSelected = state.isAllCategorySelected,
                onAllClick = { onIntent(TransactionsIntent.OnCategoriesSelected(emptySet(), isAllCategorySelected = true)) },
                onAddClick = { onIntent(TransactionsIntent.OnToggleCategorySheet) },
                chipLabel = { it.name },
                chipColor = { GlassGreen }
            )

            // Persons Section
            MultiSelectSection(
                title = stringResource(Res.string.persons),
                items = state.selectedPerson.toList(),
                isAllSelected = state.isAllPersonSelected,
                onAllClick = { onIntent(TransactionsIntent.OnPersonSelected(emptySet(), isAllPersonSelected = true)) },
                onAddClick = { onIntent(TransactionsIntent.OnTogglePersonSheet) },
                chipLabel = { it.name },
                chipColor = { GlassPurple }
            )

            // Tags Section
            MultiSelectSection(
                title = stringResource(Res.string.tags),
                items = state.selectedTag.toList(),
                isAllSelected = state.isAllTAgSelected,
                onAllClick = { onIntent(TransactionsIntent.OnTagSelected(emptySet(), isAllTAgSelected = true)) },
                onAddClick = { onIntent(TransactionsIntent.OnToggleTagSheet) },
                chipLabel = { it.name },
                chipColor = { GlassAmber }
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

@Composable
private fun <T> MultiSelectSection(
    title: String,
    items: List<T>,
    isAllSelected: Boolean,
    onAllClick: () -> Unit,
    onAddClick: () -> Unit,
    chipLabel: (T) -> String,
    chipColor: (T) -> Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackLabelSmallText(
                text = title,
                color = GlassText2,
                fontWeight = FontWeight.Bold
            )
            FintrackLabelSmallText(
                text = stringResource(Res.string.all),
                color = if (isAllSelected) GlassGreen else GlassText3,
                modifier = Modifier.clickable(onClick = onAllClick)
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                Chip(
                    active = true,
                    color = chipColor(item),
                    onClick = onAddClick // Re-open selection sheet to modify
                ) {
                    FintrackLabelSmallText(
                        text = "${chipLabel(item)} ✓",
                        color = GlassBg0
                    )
                }
            }
            
            Chip(
                active = false,
                dashed = true,
                onClick = onAddClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GlassText3,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
