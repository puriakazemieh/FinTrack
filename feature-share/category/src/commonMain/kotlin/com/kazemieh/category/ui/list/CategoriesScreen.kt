package com.kazemieh.category.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.category.ui.delete.DeleteCategoryBottomSheet
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.title_category_management
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onNavigateToTransactions: ((com.kazemieh.common.model.Category) -> Unit)? = null,
    viewModel: CategoryViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(
            CategoryIntent.LoadCategoryByType(
                TransactionType.INCOME,
                isHierarchical = true
            )
        )
    }

    val state by viewModel.state.collectAsState()

    FintrackScreen(
        title = stringResource(Res.string.category),
        sub = stringResource(Res.string.title_category_management),
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TypeSwitcher(
                selectedType = state.type,
                onTypeSelected = {
                    viewModel.onIntent(
                        CategoryIntent.LoadCategoryByType(
                            it,
                            isHierarchical = true
                        )
                    )
                }
            )

            val entityItems =
                remember(state.categories, state.expandedCategoryIds, state.allCategories) {
                    state.categories.map {
                        val hasChildren =
                            state.allCategories.any { child -> child.parentId == it.id }
                        val isExpanded = state.expandedCategoryIds.contains(it.id)
                        EntityItem(
                            id = it.id ?: 0,
                            name = it.name,
                            iconId = it.iconId,
                            colorId = it.colorId,
                            parentId = it.parentId,
                            badge = if (hasChildren && it.parentId == null) (if (isExpanded) "−" else "+") else null
                        )
                    }
                }

            EntityList(
                title = stringResource(Res.string.category),
                query = state.query,
                onQueryChange = { viewModel.onIntent(CategoryIntent.SetQuery(it)) },
                onAddClick = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
                items = entityItems,
                showActions = !state.isReorderShow,
                isReorderMode = state.isReorderShow,
                indentSubCategories = true,
                onMove = { from, to ->
                    val list = state.categories.toMutableList()
                    list.add(to, list.removeAt(from))
                    val positions = list.mapIndexed { index, category ->
                        category.id!! to index
                    }.toMap()
                    viewModel.onIntent(CategoryIntent.UpdatePositions(positions))
                },
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.categories.find { it.id == item.id }?.let { callback(it) }
                    }
                },
                onEditClick = { viewModel.onIntent(CategoryIntent.OnEditClick(state.categories.find { c -> c.id == it.id })) },
                onDeleteClick = { viewModel.onIntent(CategoryIntent.OnDeleteClick(state.categories.find { c -> c.id == it.id })) },
                onExpandClick = { item ->
                    viewModel.onIntent(CategoryIntent.ToggleExpand(item.id))
                },
                onItemClick = { item ->
                    state.categories.find { it.id == item.id }?.let { category ->
                        viewModel.onIntent(CategoryIntent.SelectedCategory(category))
                    }
                }
            )
        }

        if (state.isAddShow) {
            AddCategoryBottomSheet(
                transactionType = if (state.type == TransactionType.ALL) TransactionType.EXPENSE else state.type,
                selectedCategory = state.selectedCategory,
                onDismiss = { viewModel.onIntent(CategoryIntent.OnAddCategoryClick) },
                onNavigateToTransactions = onNavigateToTransactions,
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
}
