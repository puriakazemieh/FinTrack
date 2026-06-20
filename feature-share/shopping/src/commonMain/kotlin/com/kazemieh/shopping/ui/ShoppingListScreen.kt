package com.kazemieh.shopping.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.ItemSelected
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_to_shopping_list
import fintrack.core.designsystem.generated.resources.estimated_price
import fintrack.core.designsystem.generated.resources.priority_high
import fintrack.core.designsystem.generated.resources.priority_normal
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.total_sum
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    FintrackScreen(
        title = stringResource(Res.string.shopping_list),
        onBack = onBack,
        trailingContent = {
            val total = state.items.sumOf { it.estimatedPrice }
            Column(horizontalAlignment = Alignment.End) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.total_sum)
                )
                MoneyText(
                    amount = total.toLong()
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ShoppingListContent(
                modifier = Modifier.weight(1f),
                items = state.items,
                onIntent = viewModel::onIntent
            )

            QuickAddBar(
                name = state.newItemName,
                price = state.newItemEstimatedPrice,
                priority = state.newItemPriority,
                onNameChange = { viewModel.onIntent(ShoppingIntent.OnNewItemNameChanged(it)) },
                onPriceChange = { viewModel.onIntent(ShoppingIntent.OnNewItemPriceChanged(it)) },
                onPriorityChange = { viewModel.onIntent(ShoppingIntent.OnNewItemPriorityChanged(it)) },
                onAdd = { viewModel.onIntent(ShoppingIntent.OnAddItem) }
            )
        }
    }
}

@Composable
private fun ShoppingListContent(
    modifier: Modifier = Modifier,
    items: List<ShoppingItem>,
    onIntent: (ShoppingIntent) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val state = rememberReorderableLazyColumnState(lazyListState) { from, to ->
        onIntent(ShoppingIntent.OnReorder(from.index, to.index))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ReorderableItem(state, key = item.id) {
                ShoppingItemRow(
                    item = item,
                    onToggle = { onIntent(ShoppingIntent.OnToggleItem(item)) },
                    onDelete = { onIntent(ShoppingIntent.OnDeleteItem(item.id)) },
                    modifier = Modifier.draggableHandle()
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() }
            )

            FintrackBodyLargeText(
                text = item.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clickable { onToggle() },
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            if (item.estimatedPrice > 0) {
                MoneyText(
                    amount = item.estimatedPrice.toLong(),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (item.priority > 0) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAddBar(
    name: String,
    price: String,
    priority: Int,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onAdd: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FintrackLabelMediumText(text = stringResource(Res.string.shopping_list))
                    FintrackOutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.add_to_shopping_list)) },
                        label = { FintrackLabelSmallText(text = stringResource(Res.string.shopping_list)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(onClick = onAdd) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(0.5f)) {
                    FintrackLabelMediumText(text = stringResource(Res.string.estimated_price))
                    FintrackOutlinedTextField(
                        value = price,
                        onValueChange = onPriceChange,
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.estimated_price)) },
                        label = { FintrackLabelSmallText(text = stringResource(Res.string.estimated_price)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.weight(0.5f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ItemSelected(
                        isSelected = priority == 1,
                        item = ItemUi(
                            id = priority.toLong(),
                            title = UiText.DynamicString(if (priority == 1) stringResource(Res.string.priority_high) else stringResource(Res.string.priority_normal))
                        ),
                        onToggle = { onPriorityChange(if (priority == 1) 0 else 1) }
                    )
                }
            }
        }
    }
}
