package com.kazemieh.category.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.category.ui.delete.DeleteCategoryBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.Chip
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.type_expense
import fintrack.core.designsystem.generated.resources.type_income
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryFilterSelectionContent(
    viewModel: CategoryViewModel = koinViewModel(key = "CategoryFilterSelectionContent"),
    selectedCategories: Set<Category>,
    selectedTransactionType: TransactionType,
    isAllSelected: Boolean,
    onSelectionChanged: (Set<Category>, Boolean) -> Unit
) {
    if (selectedTransactionType == TransactionType.TRANSFER) return

    val glassColors = LocalGlassColors.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(TransactionType.ALL))
    }

    val state by viewModel.state.collectAsState()

    val showIncome =
        selectedTransactionType == TransactionType.ALL || selectedTransactionType == TransactionType.INCOME
    val showExpense =
        selectedTransactionType == TransactionType.ALL || selectedTransactionType == TransactionType.EXPENSE

    val incomeCategories =
        if (showIncome) state.categories.filter { it.type == TransactionType.INCOME } else emptyList()
    val expenseCategories =
        if (showExpense) state.categories.filter { it.type == TransactionType.EXPENSE } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackLabelSmallText(
                text = stringResource(Res.string.all),
                color = if (isAllSelected) GlassGreen else glassColors.text,
                modifier = Modifier.clickable {
                    val all = state.categories.toSet()
                    onSelectionChanged(if (isAllSelected) emptySet() else all, !isAllSelected)
                }
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = GlassGreen,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) }
            )
        }

        if (incomeCategories.isNotEmpty()) {
            FintrackLabelSmallText(text = stringResource(Res.string.type_income))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                incomeCategories.forEach { category ->
                    val active = isAllSelected || selectedCategories.contains(category)
                    Chip(
                        active = active,
                        color = GlassGreen,
                        onClick = {
                            val fullSet = state.categories.toSet()
                            val currentSelected = if (isAllSelected) fullSet else selectedCategories
                            val newSet = currentSelected.toMutableSet()

                            if (newSet.contains(category)) {
                                newSet.remove(category)
                            } else {
                                newSet.add(category)
                            }

                            val isAllNow = newSet.size == fullSet.size
                            onSelectionChanged(if (isAllNow) emptySet() else newSet, isAllNow)
                        },
                        onLongClick = { viewModel.onIntent(CategoryIntent.OnEditClick(category)) }
                    ) {
                        FintrackLabelSmallText(
                            text = category.name + if (active) " ✓" else "",
                            color = if (active) glassColors.text3 else glassColors.text
                        )
                    }
                }
            }
        }

        if (expenseCategories.isNotEmpty()) {
            FintrackLabelSmallText(text = stringResource(Res.string.type_expense))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                expenseCategories.forEach { category ->
                    val active = isAllSelected || selectedCategories.contains(category)
                    Chip(
                        active = active,
                        color = GlassRed,
                        onClick = {
                            val fullSet = state.categories.toSet()
                            val currentSelected = if (isAllSelected) fullSet else selectedCategories
                            val newSet = currentSelected.toMutableSet()

                            if (newSet.contains(category)) {
                                newSet.remove(category)
                            } else {
                                newSet.add(category)
                            }

                            val isAllNow = newSet.size == fullSet.size
                            onSelectionChanged(if (isAllNow) emptySet() else newSet, isAllNow)
                        },
                        onLongClick = { viewModel.onIntent(CategoryIntent.OnEditClick(category)) }
                    ) {
                        FintrackLabelSmallText(
                            text = category.name + if (active) " ✓" else "",
                            color = if (active) glassColors.text3 else glassColors.text
                        )
                    }
                }
            }
        }
    }

    if (state.isAddShow) {
        AddCategoryBottomSheet(
            transactionType = if (state.type == TransactionType.ALL) TransactionType.EXPENSE else state.type,
            selectedCategory = state.selectedCategory,
            onDismiss = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
            setCategory = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) }
        )
    }

    if (state.isDeleteShow && state.selectedCategory != null) {
        DeleteCategoryBottomSheet(
            category = state.selectedCategory!!,
            onDismiss = { viewModel.onIntent(CategoryIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(CategoryIntent.OnDeleteClick()) },
        )
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
