package com.kazemieh.designsystem.component.list.selectable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.list.ItemUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableFlowRowBottomSheet(
    title: String,
    items: List<ItemUi>,
    initialSelection: Set<Int> = emptySet(),
    onConfirm: (selectedIds: Set<Int>) -> Unit,
    onAddClick: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit = {}
) {

    val viewModel = remember { SelectableListViewModel() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(items, initialSelection) {
        viewModel.onIntent(SelectableIntent.Load(items, initialSelection, false))
    }

    LaunchedEffect(Unit) {
        viewModel.oneShot.collect { one ->
            when (one) {
                is SelectableOneShot.Confirmed -> {
                    onConfirm(one.selectedId)
                }

                is SelectableOneShot.Dismissed -> onDismiss()
                is SelectableOneShot.AddClick -> onAddClick()
            }
        }
    }

    SelectableFlowRowBottomSheetStateless(
        title = title,
        state = state,
        onToggle = { id -> viewModel.onIntent(SelectableIntent.Toggle(id)) },
        onConfirm = { viewModel.onIntent(SelectableIntent.Confirm) },
        onDismiss = { viewModel.onIntent(SelectableIntent.Dismiss) },
        onAddClick = { viewModel.onIntent(SelectableIntent.AddClick) },
        content = content
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableFlowRowBottomSheetStateless(
    title: String,
    state: SelectableState,
    onToggle: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    content: @Composable () -> Unit = {}
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

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.items.isNotEmpty()) {
                        state.items.forEach { item ->
                            val isSelected = state.selectedIds.contains(item.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggle(item.id) },
                                label = {
                                    FintrackBodyMediumText(
                                        text = item.title,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    } else EmptyListScreen()
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    FloatingActionButton(
                        onClick = { onAddClick() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomStart),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_person)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(
                        text = stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.background
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

            content()
        }
    }
}
