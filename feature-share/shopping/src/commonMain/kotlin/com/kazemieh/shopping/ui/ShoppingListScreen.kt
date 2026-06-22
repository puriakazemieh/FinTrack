package com.kazemieh.shopping.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ItemSelected
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_to_shopping_list
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.estimated_price
import fintrack.core.designsystem.generated.resources.priority_high
import fintrack.core.designsystem.generated.resources.priority_normal
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.total_sum
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    FintrackScreen(
        title = stringResource(Res.string.shopping_list),
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val total = state.items.sumOf { it.estimatedPrice }
            
            EntityList(
                title = stringResource(Res.string.shopping_list),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(ShoppingIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(ShoppingIntent.OnAddItem) },
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.total_sum),
                        value = total.toLong().toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.primary
                    )
                ),
                items = state.filteredItems.map {
                    EntityItem(
                        id = it.id,
                        name = it.name,
                        sub = if (it.estimatedPrice > 0) it.estimatedPrice.toLong().toPersianPrice() + " " + stringResource(Res.string.currency_toman) else null,
                        badge = if (it.priority > 0) "!" else null,
                        color = if (it.isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                    )
                },
                onItemClick = { item ->
                    state.items.find { it.id == item.id }?.let {
                        viewModel.onIntent(ShoppingIntent.OnToggleItem(it))
                    }
                },
                onEditClick = { /* TODO */ },
                onDeleteClick = { viewModel.onIntent(ShoppingIntent.OnDeleteItem(it.id)) },
                showActions = true,
                modifier = Modifier.weight(1f)
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
