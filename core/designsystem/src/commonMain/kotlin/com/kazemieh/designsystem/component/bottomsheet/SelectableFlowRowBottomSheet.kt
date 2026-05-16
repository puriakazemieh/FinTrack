package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.asString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.confirm
import org.jetbrains.compose.resources.stringResource

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val space = LocalSpacing.current


    val itemsList = remember(items) { items.toList().sortedBy { it.id } }

    var selected by remember(items, initialSelection) {
        mutableStateOf(initialSelection.intersect(items))
    }

    LaunchedEffect(initialSelection, items) {
        selected = initialSelection.intersect(items)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
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
                    if (itemsList.isEmpty()) {
                        EmptyListScreen(title)
                    } else {
                        itemsList.forEach { item ->
                            val isSelected = selected.contains(item)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selected = if (isSelected) selected - item else selected + item
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (item.iconId != null || item.colorId != null) {
                                            FinTrackLeadingIcon(
                                                colorId = item.colorId,
                                                iconId = item.iconId,
                                                style = LeadingIconStyle.TintOnly,
                                                iconSize = space.large
                                            )
                                            Spacer(Modifier.width(space.extraSmall))
                                        }

                                        FintrackBodyMediumText(
                                            text = item.title.asString(),
                                            color = if (isSelected) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(space.mediumLarge))

                FAB(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(space.small),
                    onClick = onAddClick
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onConfirm(selected) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.confirm),
                        color = MaterialTheme.colorScheme.background
                    )
                }

                Spacer(Modifier.height(space.mediumSmall))
            }

            content()
        }
    }
}
