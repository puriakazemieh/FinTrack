package com.kazemieh.tag.ui.list

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.*
import com.kazemieh.designsystem.component.glass.*
import fintrack.core.designsystem.generated.resources.tag_item
import com.kazemieh.designsystem.component.model.toTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.bottomsheet.ListBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.model.toTag
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import com.kazemieh.tag.ui.delete.DeleteTagBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.tags
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagPickerBottomSheet(
    viewModel: TagViewModel = koinViewModel(key = "TagPickerBottomSheet"),
    snackbarHostState: SnackbarHostState,
    selectedTags: Set<Tag>?,
    onSubmitClick: (Set<Tag>?) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.onIntent(TagIntent.GetAllTag) }

    LaunchedEffect(selectedTags) { viewModel.onIntent(TagIntent.SetAllSelectedTags(selectedTags)) }

    val state by viewModel.state.collectAsState()
    val initialSelection = state.initialSelectionItem.map { it.toItemUi() }.toSet()

    SelectableFlowRowBottomSheet(
        title = stringResource(Res.string.tags),
        items = state.items,
        initialSelection = initialSelection,
        onConfirm = { onSubmitClick(it.map { item -> item.toTag() }.toSet()) },
        onAddClick = { viewModel.onIntent(TagIntent.ShowAddTag) },
        onDismiss = onDismiss,
    )

    if (state.showAddTag) {
        AddTagBottomSheet(
            snackbarHostState = snackbarHostState,
            onDismiss = { viewModel.onIntent(TagIntent.ShowAddTag) },
            setTag = { viewModel.onIntent(TagIntent.SetSelectedTag(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManageBottomSheet(
    keyViewmodel: String = "TagManageBottomSheet",
    viewModel: TagViewModel = koinViewModel(key = keyViewmodel),
    snackbarHostState: SnackbarHostState,
    isDeleteShow: Boolean = true,
    isEditShow: Boolean = true,
    clickable: Boolean = false,
    onTagClick: (Tag) -> Unit = {},
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.onIntent(TagIntent.GetAllTag) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TagEffect.OnDismiss -> onDismiss()
                is TagEffect.OnTagSelected -> onTagClick(effect.tag)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(TagIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        EntityList(
            title = stringResource(Res.string.tags),
            query = state.searchQuery,
            onQueryChange = { viewModel.onIntent(TagIntent.UpdateSearchQuery(it)) },
            items = state.tags.map { tag ->
                EntityItem(
                    id = tag.id ?: 0L,
                    name = tag.name,
                    sub = tag.description,
                    iconId = tag.iconId,
                    colorId = tag.colorId
                )
            },
            onItemClick = { item ->
                if (clickable) {
                    state.tags.find { it.id == item.id }?.let {
                        viewModel.onIntent(TagIntent.SelectedTag(it))
                    }
                }
            },
            onAddClick = { viewModel.onIntent(TagIntent.ShowAddTag) },
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
            summary = listOf(
                EntitySummary(
                    label = "تعداد برچسب‌ها",
                    value = state.tags.size.toString(),
                    unit = stringResource(Res.string.tag_item, ""),
                    color = MaterialTheme.colorScheme.primary
                )
            )
        )
    }

    if (state.showAddTag) {
        AddTagBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedTag = state.selectedTag,
            onDismiss = { viewModel.onIntent(TagIntent.ShowAddTag) },
            setTag = { viewModel.onIntent(TagIntent.SelectedTag(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedTag != null) {
        DeleteTagBottomSheet(
            snackbarHostState = snackbarHostState,
            tag = state.selectedTag!!,
            onDismiss = { viewModel.onIntent(TagIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(TagIntent.OnDeleteClick()) }
        )
    }
}

@Composable
fun TagSelectionBottomSheet(
    viewModel: TagViewModel = koinViewModel(key = "TagSelectionBottomSheet"),
    initialSelectionPairs: Set<Tag> = emptySet(),
    isAllSelected: Boolean = true,
    onConfirmPairs: (Set<Tag>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) { viewModel.onIntent(TagIntent.GetAllTag) }

    val initialSelectionIds = if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(Res.string.tags),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toTag() }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}
