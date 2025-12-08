package com.kazemieh.category.ui


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.designsystem.component.list.normal.ListBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.designsystem.component.list.toPairSetFrom
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(),
    selectedTransactionType: Int,
    onCategoryClick: (id: Int, name: String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(true) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(selectedTransactionType))
    }

    val state by viewModel.state.collectAsState()

    val items = state.categories.map { ItemUi(it.id?.toInt() ?: 0, it.name) }

    ListBottomSheet(
        title = stringResource(R.string.category),
        items = items,
        onConfirm = onCategoryClick,
        onAddClick = onAddCategoryClick,
        onDismiss = onDismiss
    )

}


@Composable
fun CategoryListSelectionBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(),
    initialSelectionPairs: Set<Pair<Int, String>> = emptySet(),
    onConfirmPairs: (Set<Pair<Int, String>>, isAllSelected: Boolean) -> Unit,
    selectedTransactionType: Int,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(selectedTransactionType) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(selectedTransactionType))
    }

    val initialSelectionIds = initialSelectionPairs.map { it.first }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.category),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedIds, isAll ->
            val pairSet = selectedIds.toPairSetFrom(state.items)
            onConfirmPairs(pairSet, isAll)
        },
        onDismiss = onDismiss
    )
}