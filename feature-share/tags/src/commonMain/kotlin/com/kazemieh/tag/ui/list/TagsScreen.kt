package com.kazemieh.tag.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import com.kazemieh.tag.ui.delete.DeleteTagBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_tag_management
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

    FintrackScreen(
        title = stringResource(Res.string.tags),
        sub = stringResource(Res.string.title_tag_management),
        onClose = onBack
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
                    val list = state.tags.toMutableList()
                    list.add(to, list.removeAt(from))
                    val positions = list.mapIndexed { index, tag ->
                        tag.id!! to index
                    }.toMap()
                    viewModel.onIntent(TagIntent.UpdatePositions(positions))
                },
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.tags.find { it.id == item.id }?.let { callback(it) }
                    }
                },
                items = state.filteredTags.map {
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
