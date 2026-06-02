package com.kazemieh.category.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.category.ui.delete.DeleteCategoryBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.model.toCategory
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import fintrack.core.designsystem.generated.resources.*
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
        containerColor = MaterialTheme.colorScheme.background
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.category),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            
            EntityList(
                title = stringResource(Res.string.category),
                query = state.query,
                onQueryChange = { viewModel.onIntent(CategoryIntent.SetQuery(it)) },
                onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
                summary = state.summaries,
                items = entityItems,
                onEditClick = { viewModel.onIntent(CategoryIntent.OnEditClick(state.categories.find { c -> c.id == it.id })) },
                onDeleteClick = { viewModel.onIntent(CategoryIntent.OnDeleteClick(state.categories.find { c -> c.id == it.id })) },
                onItemClick = { item ->
                    state.categories.find { it.id == item.id }?.let { onCategoryClick(it) }
                }
            )
        }
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
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(TransactionType.ALL))
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

    SheetFrame(
        title = stringResource(Res.string.category),
        onDismiss = onDismiss
    ) {
        TopCategoryContent(state.listTransactionType, state.type) {
            viewModel.onIntent(CategoryIntent.LoadCategoryByType(it))
        }

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
            summary = state.summaries,
            items = entityItems,
            onEditClick = { viewModel.onIntent(CategoryIntent.OnEditClick(state.categories.find { c -> c.id == it.id })) },
            onDeleteClick = { viewModel.onIntent(CategoryIntent.OnDeleteClick(state.categories.find { c -> c.id == it.id })) }
        )
    }

    if (state.isAddShow) {
        AddCategoryBottomSheet(
            snackbarHostState = snackbarHostState,
            transactionType = state.type,
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
fun TopCategoryContent(
    listTransactionType: List<TransactionType>,
    type: TransactionType,
    onConfirm: (TransactionType) -> Unit
) {
    val space = LocalSpacing.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = space.mediumSmall)
        ) {
            listTransactionType.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = type == option,
                    onClick = { onConfirm(option) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = listTransactionType.size
                    ),
                ) {
                    val text = when (option.count) {
                        1 -> stringResource(Res.string.incoming_category)
                        2 -> stringResource(Res.string.outcoming_category)
                        else -> ""
                    }
                    FintrackBodyMediumText(text = text)
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

    val initialSelectionIds = if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

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
