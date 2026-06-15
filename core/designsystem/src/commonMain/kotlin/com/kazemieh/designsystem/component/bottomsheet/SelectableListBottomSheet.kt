package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.EmptyList
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.ItemSelected
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.search_placeholder
import fintrack.core.designsystem.generated.resources.select_All
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableListBottomSheet(
    title: String,
    items: Set<ItemUi>,
    initialSelection: Set<ItemUi> = emptySet(),
    showSelectAll: Boolean = true,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onConfirm: (selectedIds: Set<ItemUi>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val space = LocalSpacing.current

    val itemsList = remember(items) { items.toList().sortedBy { it.id } }

    var selected by remember(items, initialSelection) {
        mutableStateOf(initialSelection.intersect(items))
    }

    LaunchedEffect(items, initialSelection) {
        selected = initialSelection.intersect(items)
    }

    val isAllSelected = items.isNotEmpty() && selected.size == items.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        val glassColors = LocalGlassColors.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(glassColors.bg1, glassColors.bg0)))
        ) {
            ScreenHeader(
                title = title,
                onClose = onDismiss
            )

            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(Res.string.search_placeholder),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (showSelectAll && itemsList.size > 1) {
                val text = stringResource(Res.string.select_All)
                val allItem = ItemUi(id = 0, title = UiText.DynamicString(text))

                ItemSelected(
                    modifier = Modifier.padding(
                        vertical = space.mediumSmall,
                        horizontal = 24.dp
                    ),
                    isSelected = isAllSelected,
                    item = allItem,
                    onToggle = {
                        selected = if (isAllSelected) emptySet() else items
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
            }

            if (itemsList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(itemsList, key = { it.id }) { item ->
                        val isSelected = selected.contains(item)
                        ItemSelected(
                            modifier = Modifier.padding(
                                vertical = space.mediumSmall,
                                horizontal = 24.dp
                            ),
                            isSelected = isSelected,
                            item = item,
                            onToggle = {
                                selected = if (isSelected) selected - item else selected + item
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    EmptyList(title)
                }
            }

            // Fixed CTA at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { onConfirm(selected, isAllSelected) }
                ) {
                    FintrackBodyMediumText(text = stringResource(Res.string.confirm))
                }
            }
        }
    }
}
