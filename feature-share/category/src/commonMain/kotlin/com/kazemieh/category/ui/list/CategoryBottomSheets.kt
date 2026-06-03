package com.kazemieh.category.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.category.ui.delete.DeleteCategoryBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.model.toCategory
import com.kazemieh.designsystem.component.model.toItemUi
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.label_expense
import fintrack.core.designsystem.generated.resources.label_income
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(key = "CategoryPickerBottomSheet"),
    snackbarHostState: SnackbarHostState,
    transactionType: TransactionType,
    onCategoryClick: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(transactionType) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(transactionType))
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CategoryEffect.AddedCategory -> onCategoryClick(effect.category)
                CategoryEffect.OnDismiss -> onDismiss()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(CategoryIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        val entityItems = remember(state.categories) {
            state.categories.map {
                EntityItem(
                    id = it.id ?: 0,
                    name = it.name,
                    iconId = it.iconId,
                    colorId = it.colorId
                )
            }
        }

        EntityList(
            title = stringResource(Res.string.category),
            query = state.query,
            onQueryChange = { viewModel.onIntent(CategoryIntent.SetQuery(it)) },
            onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
            items = entityItems,
            onEditClick = { viewModel.onIntent(CategoryIntent.OnEditClick(state.categories.find { c -> c.id == it.id })) },
            onDeleteClick = { viewModel.onIntent(CategoryIntent.OnDeleteClick(state.categories.find { c -> c.id == it.id })) },
            onItemClick = { item ->
                state.categories.find { it.id == item.id }?.let { onCategoryClick(it) }
            }
        )
    }

    if (state.isAddShow) {
        AddCategoryBottomSheet(
            snackbarHostState = snackbarHostState,
            transactionType = transactionType,
            onDismiss = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
            setCategory = { viewModel.onIntent(CategoryIntent.SelectedCategory(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(key = "CategoryManageBottomSheet"),
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(TransactionType.INCOME))
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CategoryEffect.OnDismiss -> onDismiss()
                else -> {}
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(CategoryIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TypeSwitcher(
                selectedType = state.type,
                onTypeSelected = { viewModel.onIntent(CategoryIntent.LoadCategoryByType(it)) }
            )

            val entityItems = remember(state.categories) {
                state.categories.map {
                    EntityItem(
                        id = it.id ?: 0,
                        name = it.name,
                        iconId = it.iconId,
                        colorId = it.colorId
                    )
                }
            }

            EntityList(
                title = stringResource(Res.string.category),
                query = state.query,
                onQueryChange = { viewModel.onIntent(CategoryIntent.SetQuery(it)) },
                onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
                items = entityItems,
                onEditClick = { viewModel.onIntent(CategoryIntent.OnEditClick(state.categories.find { c -> c.id == it.id })) },
                onDeleteClick = { viewModel.onIntent(CategoryIntent.OnDeleteClick(state.categories.find { c -> c.id == it.id })) }
            )
        }
    }

    if (state.isAddShow) {
        AddCategoryBottomSheet(
            snackbarHostState = snackbarHostState,
            transactionType = if (state.type == TransactionType.ALL) TransactionType.EXPENSE else state.type,
            selectedCategory = state.selectedCategory,
            onDismiss = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
            setCategory = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) }
        )
    }

    if (state.isDeleteShow && state.selectedCategory != null) {
        DeleteCategoryBottomSheet(
            snackbarHostState = snackbarHostState,
            category = state.selectedCategory!!,
            onDismiss = { viewModel.onIntent(CategoryIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(CategoryIntent.OnDeleteClick()) },
        )
    }
}

@Composable
private fun TypeSwitcher(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    val types = listOf(
        TransactionType.EXPENSE to stringResource(Res.string.label_expense),
        TransactionType.INCOME to stringResource(Res.string.label_income)
    )

    GlassCard(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        padding = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            types.forEach { (type, label) ->
                val active =
                    if (selectedType == TransactionType.ALL) type == TransactionType.EXPENSE else type == selectedType
                val color = if (type == TransactionType.INCOME) GlassGreen else GlassRed

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (active) color.copy(alpha = 0.12f) else Color.Transparent)
                        .then(
                            if (active) Modifier.border(
                                1.dp,
                                color.copy(alpha = 0.33f),
                                CircleShape
                            )
                            else Modifier
                        )
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FintrackLabelMediumText(
                        text = label,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) color else GlassText3
                    )
                }
            }
        }
    }
}


@Composable
fun CategorySelectionBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(key = "CategorySelectionBottomSheet"),
    initialSelectionPairs: Set<Category> = emptySet(),
    isAllSelected: Boolean = true,
    onConfirmPairs: (Set<Category>, isAllSelected: Boolean) -> Unit,
    selectedTransactionType: TransactionType,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(selectedTransactionType) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(selectedTransactionType))
    }

    val initialSelectionIds =
        if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(Res.string.category),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toCategory() }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}
