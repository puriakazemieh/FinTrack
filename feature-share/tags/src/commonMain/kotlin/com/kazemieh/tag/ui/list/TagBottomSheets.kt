package com.kazemieh.tag.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.bottomsheet.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.model.toTag
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import com.kazemieh.tag.ui.delete.DeleteTagBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.edit
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
    onDismiss: () -> Unit,
    isEditShow: Boolean = false,
    showEditButton: Boolean = true
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
        isEditShow = isEditShow,
        showEditButton = showEditButton,
        onEditClick = { item ->
            state.tags.find { it.id == item.id }?.let {
                viewModel.onIntent(TagIntent.OnEditClick(it))
            }
        },
        onDeleteClick = { item ->
            state.tags.find { it.id == item.id }?.let {
                viewModel.onIntent(TagIntent.OnDeleteClick(it))
            }
        }
    )

    if (state.showAddTag) {
        AddTagBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedTag = state.selectedTag,
            onDismiss = { viewModel.onIntent(TagIntent.ResetFlags) },
            onNavigateToTransactions = null,
            setTag = { viewModel.onIntent(TagIntent.SetSelectedTag(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedTag != null) {
        DeleteTagBottomSheet(
            snackbarHostState = snackbarHostState,
            tag = state.selectedTag!!,
            onDismiss = { viewModel.onIntent(TagIntent.ResetFlags) },
            deleted = { viewModel.onIntent(TagIntent.ResetFlags) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPickerSingleBottomSheet(
    viewModel: TagViewModel = koinViewModel(key = "TagPickerSingleBottomSheet"),
    snackbarHostState: SnackbarHostState,
    onTagClick: (Tag) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Tag) -> Unit)? = null,
    isEditShow: Boolean = false,
    showEditButton: Boolean = true
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    val state by viewModel.state.collectAsState()
    var isEditMode by remember { mutableStateOf(isEditShow) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TagEffect.OnTagSelected -> onTagClick(effect.tag)
                TagEffect.OnDismiss -> onDismiss()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(TagIntent.ResetFlags)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = stringResource(Res.string.tags),
                onClose = {
                    viewModel.onIntent(TagIntent.ResetFlags)
                    onDismiss()
                },
                trailingContent = {
                    if (showEditButton) {
                        TextButton(onClick = { isEditMode = !isEditMode }) {
                            FintrackLabelMediumText(
                                text = if (isEditMode) stringResource(Res.string.cancell_) else stringResource(Res.string.edit),
                                color = GlassText2
                            )
                        }
                    }
                }
            )

            EntityList(
                title = stringResource(Res.string.tags),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(TagIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(TagIntent.ShowAddTag) },
                showActions = isEditMode,
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.tags.find { it.id == item.id }?.let { callback(it) }
                        onDismiss()
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
                onItemClick = { item ->
                    state.tags.find { it.id == item.id }?.let { onTagClick(it) }
                }
            )
        }
    }

    if (state.showAddTag) {
        AddTagBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedTag = state.selectedTag,
            onDismiss = { viewModel.onIntent(TagIntent.ResetFlags) },
            onNavigateToTransactions = onNavigateToTransactions?.let { navCallback ->
                { tag ->
                    navCallback(tag)
                    onDismiss()
                }
            },
            setTag = { viewModel.onIntent(TagIntent.SetSelectedTag(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedTag != null) {
        DeleteTagBottomSheet(
            snackbarHostState = snackbarHostState,
            tag = state.selectedTag!!,
            onDismiss = { viewModel.onIntent(TagIntent.ResetFlags) },
            deleted = { viewModel.onIntent(TagIntent.ResetFlags) }
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
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Tag) -> Unit)? = null
) = TagPickerSingleBottomSheet(
    viewModel = viewModel,
    snackbarHostState = snackbarHostState,
    onTagClick = onTagClick,
    onDismiss = onDismiss,
    onNavigateToTransactions = onNavigateToTransactions,
    isEditShow = isEditShow,
    showEditButton = false
)

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

    val initialSelectionIds =
        if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(Res.string.tags),
        items = state.items,
        initialSelection = initialSelectionIds,
        query = state.searchQuery,
        onQueryChange = { viewModel.onIntent(TagIntent.UpdateSearchQuery(it)) },
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toTag() }.toSet(), isAll)
            viewModel.onIntent(TagIntent.OnDismiss)
        },
        onDismiss = {
            viewModel.onIntent(TagIntent.OnDismiss)
        }
    )
}
