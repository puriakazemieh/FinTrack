package com.kazemieh.category.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.category.ui.delete.DeleteCategoryBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.model.toCategory
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.bottomsheet.ListBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import org.koin.androidx.compose.koinViewModel

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

    ListBottomSheet(
        title = stringResource(R.string.category),
        items = state.items,
        onItemClicked = { viewModel.onIntent(CategoryIntent.SelectedCategory(it.toCategory())) },
        onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
        onDismiss = { viewModel.onIntent(CategoryIntent.OnDismiss) }
    )

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
        viewModel.onIntent(CategoryIntent.LoadCategoryByType())
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

    ListBottomSheet(
        title = stringResource(R.string.category),
        items = state.items,
        isShowTopContent = true,
        isDeleteShow = true,
        isEditShow = true,
        onItemEditClicked = { viewModel.onIntent(CategoryIntent.OnEditClick(it.toCategory())) },
        onItemDeleteClicked = { viewModel.onIntent(CategoryIntent.OnDeleteClick(it.toCategory())) },
        onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
        topContent = {
            TopCategoryContent(state.listTransactionType, state.type) {
                viewModel.onIntent(CategoryIntent.LoadCategoryByType(it))
            }
        },
        onDismiss = { viewModel.onIntent(CategoryIntent.OnDismiss) }
    )

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
                        1 -> stringResource(R.string.incoming_category)
                        2 -> stringResource(R.string.outcoming_category)
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
    onConfirmPairs: (Set<Category>, isAllSelected: Boolean) -> Unit,
    selectedTransactionType: TransactionType,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(selectedTransactionType) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(selectedTransactionType))
    }

    val initialSelectionIds = initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.category),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toCategory() }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}
