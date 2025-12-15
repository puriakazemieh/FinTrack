package com.kazemieh.category.ui


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.toCategory
import com.kazemieh.common.model.toItemUi
import com.kazemieh.common.model.toPairSetFrom
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.list.normal.ListBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(),
    transactionType: TransactionType,
    onCategoryClick: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(transactionType) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(transactionType))
    }

    val state by viewModel.state.collectAsState()

    ListBottomSheet(
        title = stringResource(R.string.category),
        items = state.items,
        onConfirm = { onCategoryClick(it.toCategory()) },
        onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
        onDismiss = onDismiss
    )

    AddCategoryBottomSheet(
        transactionType = transactionType,
        onDismiss = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
        setCategory = { onCategoryClick(it) }
    )

}


@Composable
fun CategoryListSelectionBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(),
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