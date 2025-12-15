package com.kazemieh.designsystem.component.list.selectable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.ItemUi
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableListBottomSheet(
    title: String,
    items: Set<ItemUi>,
    initialSelection: Set<ItemUi> = emptySet(),
    showSelectAll: Boolean = true,
    onConfirm: (selectedIds: Set<ItemUi>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = remember { SelectableListViewModel() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(items, initialSelection, showSelectAll) {
        viewModel.onIntent(SelectableIntent.Load(items, initialSelection, showSelectAll))
    }

    LaunchedEffect(Unit) {
        viewModel.oneShot.collect { one ->
            when (one) {
                is SelectableOneShot.Confirmed -> {
                    onConfirm(one.selectedItems, one.isAllSelected)
                }

                is SelectableOneShot.Dismissed -> onDismiss()
                else -> {}
            }
        }
    }

    SelectableListBottomSheetStateless(
        title = title,
        state = state,
        onToggle = { viewModel.onIntent(SelectableIntent.Toggle(it)) },
        onToggleSelectAll = { viewModel.onIntent(SelectableIntent.ToggleSelectAll) },
        onConfirm = { viewModel.onIntent(SelectableIntent.Confirm) },
        onDismiss = { viewModel.onIntent(SelectableIntent.Dismiss) },
        showSelectAll = showSelectAll
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableListBottomSheetStateless(
    title: String,
    state: SelectableState,
    onToggle: (ItemUi) -> Unit,
    onToggleSelectAll: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showSelectAll: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FintrackTitleLargeText(
                    text = title,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(Modifier.height(8.dp))

                if (showSelectAll && state.items.size > 1) {
                    val text = stringResource(R.string.select_All)
                    val item = ItemUi(id = 0, title = text)
                    ItemSelected(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        isSelected = state.isAllSelected,
                        item = item,
                        onToggle = { onToggleSelectAll() }
                    )
                }

                HorizontalDivider()

                if (!state.items.isEmpty()) {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(state.items.toList()) { item ->
                            val isSelected = state.selectedItems.contains(item)
                            ItemSelected(
                                modifier = Modifier.padding(12.dp),
                                isSelected = isSelected,
                                item = item,
                                onToggle = { onToggle(item) })
                        }
                    }
                } else EmptyListScreen()

                Spacer(Modifier.height(12.dp))

                Button(modifier = Modifier.fillMaxWidth(), onClick = onConfirm) {
                    FintrackBodyMediumText(text = stringResource(R.string.confirm))
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


@Composable
fun ItemSelected(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    item: ItemUi,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(12.dp))
        FintrackBodyMediumText(text = item.title)
    }

}
