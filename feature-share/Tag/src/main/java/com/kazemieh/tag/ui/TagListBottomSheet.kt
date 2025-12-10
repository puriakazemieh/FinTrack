package com.kazemieh.tag.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.designsystem.component.list.selectable.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.designsystem.component.list.toPairSetFrom
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagListBottomSheet(
    viewModel: TagViewModel = koinViewModel(),
    selectedTags: Set<Pair<Int, String>>?,
    onSubmitClick: (Set<Pair<Int, String>>?) -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(true) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    LaunchedEffect(selectedTags) {
        viewModel.onIntent(TagIntent.SetAllSelectedTags(selectedTags))
    }

    val state by viewModel.state.collectAsState()

    SelectableFlowRowBottomSheet(
        title = stringResource(R.string.tags),
        items = state.items,
        initialSelection = state.initialSelectionIds,
        onConfirm = { selectedIds ->
            val pairSet = selectedIds.toPairSetFrom(state.items)
            onSubmitClick(pairSet)
        },
        onAddClick = { viewModel.onIntent(TagIntent.ShowAddTag) },
        onDismiss = onDismiss,
    )

    if (state.showAddTag) {
        AddTagBottomSheet(
            onDismiss = { viewModel.onIntent(TagIntent.ShowAddTag) },
            setTag = { id, name -> viewModel.onIntent(TagIntent.SetSelectedTag(id to name)) }
        )
    }

}


@Composable
fun TagListSelectionBottomSheet(
    viewModel: TagViewModel = koinViewModel(),
    initialSelectionPairs: Set<Pair<Int, String>> = emptySet(),
    onConfirmPairs: (Set<Pair<Int, String>>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    val items = state.tags.map { ItemUi(it.id?.toInt() ?: 0, it.name) }
    val initialSelectionIds = initialSelectionPairs.map { it.first }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.tags),
        items = items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedIds, isAll ->
            val pairSet = selectedIds.toPairSetFrom(items)
            onConfirmPairs(pairSet, isAll)
        },
        onDismiss = onDismiss
    )
}