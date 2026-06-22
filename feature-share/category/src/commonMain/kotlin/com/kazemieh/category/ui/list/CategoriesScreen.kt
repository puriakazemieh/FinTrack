package com.kazemieh.category.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.category.ui.delete.DeleteCategoryBottomSheet
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.HeaderAction
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.reorder_list
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
    var reorderedList by remember(state.categories) { mutableStateOf(state.categories) }

    FintrackScreen(
        title = stringResource(Res.string.category),
        sub = stringResource(Res.string.title_category_management),
        onBack = onBack,
        actions = if (state.isReorderShow) {
            listOf(
                HeaderAction(
                    icon = rememberVectorPainter(Icons.Default.Check),
                    label = "Done",
                    onClick = {
                        val positions = reorderedList.mapIndexed { index, category ->
                            category.id!! to index
                        }.toMap()
                        viewModel.onIntent(CategoryIntent.UpdatePositions(positions))
                        viewModel.onIntent(CategoryIntent.OnToggleReorder)
                    },
                    color = com.kazemieh.designsystem.GlassGreen
                )
            )
        } else {
            emptyList()
        },
        trailingContent = {
            if (!state.isReorderShow) {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.reorder_list)) },
                            onClick = {
                                viewModel.onIntent(CategoryIntent.OnToggleReorder)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
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
                remember(reorderedList, state.expandedCategoryIds, state.allCategories) {
                    reorderedList.map {
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
                    reorderedList = reorderedList.toMutableList().apply {
                        add(to, removeAt(from))
                    }
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
