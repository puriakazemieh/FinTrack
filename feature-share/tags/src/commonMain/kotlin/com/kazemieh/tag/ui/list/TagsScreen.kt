package com.kazemieh.tag.ui.list

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
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.HeaderAction
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import com.kazemieh.tag.ui.delete.DeleteTagBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TagsScreen(
    onBack: () -> Unit,
    onNavigateToTransactions: ((Tag) -> Unit)? = null,
    viewModel: TagViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    val state by viewModel.state.collectAsState()
    var reorderedList by remember(state.tags) { mutableStateOf(state.tags) }

    FintrackScreen(
        title = stringResource(Res.string.tags),
        sub = stringResource(Res.string.title_tag_management),
        onBack = onBack,
        actions = if (state.isReorderShow) {
            listOf(
                HeaderAction(
                    icon = rememberVectorPainter(Icons.Default.Check),
                    label = "Done",
                    onClick = {
                        val positions = reorderedList.mapIndexed { index, tag ->
                            tag.id!! to index
                        }.toMap()
                        viewModel.onIntent(TagIntent.UpdatePositions(positions))
                        viewModel.onIntent(TagIntent.OnToggleReorder)
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
                                viewModel.onIntent(TagIntent.OnToggleReorder)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.tags),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(TagIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(TagIntent.ShowAddTag) },
                showActions = !state.isReorderShow,
                isReorderMode = state.isReorderShow,
                onMove = { from, to ->
                    reorderedList = reorderedList.toMutableList().apply {
                        add(to, removeAt(from))
                    }
                },
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.tags.find { it.id == item.id }?.let { callback(it) }
                    }
                },
                items = reorderedList.filter {
                    it.name.contains(state.searchQuery, ignoreCase = true) ||
                            it.description?.contains(state.searchQuery, ignoreCase = true) == true
                }.map {
                    EntityItem(
                        id = it.id ?: 0,
                        name = it.name,
                        sub = it.description,
                        iconId = it.iconId,
                        colorId = it.colorId
                    )
                },
                onEditClick = { item ->
                    state.tags.find { it.id == item.id }?.let {
                        viewModel.onIntent(TagIntent.OnEditClick(it))
                    }
                },
                onDeleteClick = { item ->
                    state.tags.find { it.id == item.id }?.let {
                        viewModel.onIntent(TagIntent.OnDeleteClick(it))
                    }
                },
                onItemClick = { /* No detail for tags? */ }
            )
        }

        if (state.showAddTag) {
            AddTagBottomSheet(
                selectedTag = state.selectedTag,
                onDismiss = { viewModel.onIntent(TagIntent.ResetFlags) },
                onNavigateToTransactions = onNavigateToTransactions,
                setTag = { viewModel.onIntent(TagIntent.SetSelectedTag(it)) }
            )
        }

        if (state.isDeleteShow && state.selectedTag != null) {
            DeleteTagBottomSheet(
                tag = state.selectedTag!!,
                onDismiss = { viewModel.onIntent(TagIntent.ResetFlags) },
                deleted = { viewModel.onIntent(TagIntent.ResetFlags) }
            )
        }
    }
}
