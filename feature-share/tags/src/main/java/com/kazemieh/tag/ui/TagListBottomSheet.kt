package com.kazemieh.tag.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.common.ld
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.toItemUi
import com.kazemieh.common.model.toTag
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.list.selectable.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagListBottomSheet(
    viewModel: TagViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedTags: Set<Tag>?,
    onSubmitClick: (Set<Tag>?) -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(true) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    LaunchedEffect(selectedTags) {
        viewModel.onIntent(TagIntent.SetAllSelectedTags(selectedTags))
    }

    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    val initialSelection = state.initialSelectionItem.map { it.toItemUi() }.toSet()

    SelectableFlowRowBottomSheet(
        title = stringResource(R.string.tags),
        items = state.items,
        initialSelection = initialSelection,
        onConfirm = { onSubmitClick(it.map { it.toTag(context) }.toSet()) },
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


@Composable
fun TagListSelectionBottomSheet(
    viewModel: TagViewModel = koinViewModel(),
    initialSelectionPairs: Set<Tag> = emptySet(),
    onConfirmPairs: (Set<Tag>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    val initialSelectionIds = initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.tags),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toTag(context) }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}