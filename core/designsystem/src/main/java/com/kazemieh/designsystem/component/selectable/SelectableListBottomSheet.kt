package com.kazemieh.designsystem.component.selectable

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableListBottomSheet(
    title: String,
    items: List<SelectableItemUi>,
    initialSelection: Set<Int> = emptySet(),
    showSelectAll: Boolean = true,
    onConfirm: (selectedIds: Set<Int>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = remember { SelectableListViewModel() }
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(items, initialSelection, showSelectAll) {
        viewModel.onIntent(SelectableIntent.Load(items, initialSelection, showSelectAll))
    }

    LaunchedEffect(Unit) {
        viewModel.oneShot.collect { one ->
            when (one) {
                is SelectableOneShot.Confirmed -> {
                    onConfirm(one.selectedIds, one.isAllSelected)
                }

                is SelectableOneShot.Dismissed -> onDismiss()
            }
        }
    }

    SelectableListBottomSheetStateless(
        title = title,
        state = state,
        onToggle = { id -> viewModel.onIntent(SelectableIntent.Toggle(id)) },
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
    onToggle: (Int) -> Unit,
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(Modifier.height(8.dp))

                if (showSelectAll) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSelectAll() }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.isAllSelected,
                            onCheckedChange = { onToggleSelectAll() })
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Select all")
                    }
                }

                HorizontalDivider()

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(state.items) { item ->
                        val isSelected = state.selectedIds.contains(item.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(item.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = { onToggle(item.id) })
                            Spacer(Modifier.width(12.dp))
                            Text(text = item.title)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(modifier = Modifier.fillMaxWidth(), onClick = onConfirm) {
                    Text(text = "Confirm")
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
