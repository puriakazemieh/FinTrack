package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.model.asString

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val space = LocalSpacing.current

    val itemsList = remember(items) { items.toList().sortedBy { it.id } }

    var selected by remember(items, initialSelection, showSelectAll) {
        mutableStateOf(
            if (initialSelection.isEmpty() && showSelectAll) items
            else initialSelection.intersect(items)
        )
    }

    LaunchedEffect(items, initialSelection, showSelectAll) {
        selected =
            if (initialSelection.isEmpty() && showSelectAll) items
            else initialSelection.intersect(items)
    }

    val isAllSelected = items.isNotEmpty() && selected.size == items.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.mediumLarge)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FintrackTitleLargeText(
                    text = title,
                    modifier = Modifier.padding(space.small)
                )
                Spacer(Modifier.height(space.mediumSmall))

                if (showSelectAll && itemsList.size > 1) {
                    val text = stringResource(R.string.select_All)
                    val allItem = ItemUi(id = 0, title = UiText.DynamicString(text))

                    ItemSelected(
                        modifier = Modifier.padding(
                            vertical = space.mediumSmall,
                            horizontal = space.small
                        ),
                        isSelected = isAllSelected,
                        item = allItem,
                        onToggle = {
                            selected = if (isAllSelected) emptySet() else items
                        }
                    )
                }

                HorizontalDivider()

                if (itemsList.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(itemsList, key = { it.id }) { item ->
                            val isSelected = selected.contains(item)
                            ItemSelected(
                                modifier = Modifier.padding(space.mediumLarge),
                                isSelected = isSelected,
                                item = item,
                                onToggle = {
                                    selected = if (isSelected) selected - item else selected + item
                                }
                            )
                        }
                    }
                } else {
                    EmptyListScreen(title)
                }

                Spacer(Modifier.height(space.mediumLarge))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onConfirm(selected, isAllSelected) }
                ) {
                    FintrackBodyMediumText(text = stringResource(R.string.confirm))
                }

                Spacer(Modifier.height(space.mediumSmall))
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
    val space = LocalSpacing.current


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(space.mediumLarge))
        if (item.iconId != null || item.colorId != null) {
            FinTrackLeadingIcon(
                colorId = item.colorId,
                iconId = item.iconId,
                style = LeadingIconStyle.Badge,
                size = space.extraLarge,
                iconSize = space.large,
                corner = space.mediumLarge
            )
            Spacer(modifier = Modifier.width(space.mediumSmall))
        }
        FintrackBodyMediumText(text = item.title.asString())
    }

}
