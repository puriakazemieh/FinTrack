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
import com.kazemieh.common.model.ItemUi
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableFlowRowBottomSheet(
    title: String,
    items: Set<ItemUi>,
    initialSelection: Set<ItemUi> = emptySet(),
    onConfirm: (selectedItem: Set<ItemUi>) -> Unit,
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
                is SelectableOneShot.Confirmed -> onConfirm(one.selectedItems)
                is SelectableOneShot.Dismissed -> onDismiss()
                is SelectableOneShot.AddClick -> onAddClick()
            }
        }
    }

    SelectableFlowRowBottomSheetStateless(
        title = title,
        state = state,
        onToggle = { viewModel.onIntent(SelectableIntent.Toggle(it)) },
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
    onToggle: (ItemUi) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val space = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.medium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FintrackTitleLargeText(
                    text = title,
                    modifier = Modifier.padding(space.extraSmall)
                )
                Spacer(Modifier.height(space.small))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(space.mediumSmall)
                ) {
                    if (state.items.isNotEmpty()) {
                        state.items.forEach { item ->
                            val isSelected = state.selectedItems.contains(item)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggle(item) },
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
                    } else EmptyListScreen(title)
                }

                Spacer(Modifier.height(space.mediumLarge))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(space.small)
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

                Spacer(Modifier.height(space.mediumLarge))

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

                Spacer(Modifier.height(space.mediumSmall))
            }

            content()
        }
    }
}
