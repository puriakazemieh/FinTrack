package com.kazemieh.shopping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val showDatePicker = remember { mutableStateOf(false) }

    FintrackScreen(
        title = stringResource(Res.string.shopping_list),
        onBack = onBack
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
                onEditClick = { item ->
                    state.items.find { it.id == item.id }?.let {
                        viewModel.onIntent(ShoppingIntent.OnEditItem(it))
                    }
                },
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
                onReminderClick = { showDatePicker.value = true },
                onAdd = { viewModel.onIntent(ShoppingIntent.OnAddItem) }
            )
        }
    }

    JalaliDatePickerBottomSheet(
        openSheet = showDatePicker,
        onConfirm = { calendar ->
            viewModel.onIntent(ShoppingIntent.OnNewItemReminderChanged(calendar.toTimestamp()))
        }
    )

    state.editingItem?.let { item ->
        EditShoppingItemSheet(
            item = item,
            onDismiss = { viewModel.onIntent(ShoppingIntent.OnEditItem(null)) },
            onConfirm = { updated ->
                viewModel.onIntent(ShoppingIntent.OnUpdateItem(updated))
            }
        )
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
    onReminderClick: () -> Unit,
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
                    FintrackOutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.add_to_shopping_list)) },
                        label = { FintrackLabelSmallText(text = stringResource(Res.string.shopping_list)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(onClick = onReminderClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditShoppingItemSheet(
    item: com.kazemieh.common.model.ShoppingItem,
    onDismiss: () -> Unit,
    onConfirm: (com.kazemieh.common.model.ShoppingItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.estimatedPrice.toString()) }
    var priority by remember { mutableStateOf(item.priority) }
    val showDatePicker = remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf(item.reminderTime) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = stringResource(Res.string.edit),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = {
                onConfirm(item.copy(name = name, estimatedPrice = price.toDoubleOrNull() ?: 0.0, priority = priority, reminderTime = reminderTime))
            },
            onClose = onDismiss,
            showHero = false
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FintrackOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { FintrackBodyMediumText(stringResource(Res.string.shopping_list)) },
                    modifier = Modifier.fillMaxWidth()
                )

                FintrackOutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { FintrackBodyMediumText(stringResource(Res.string.estimated_price)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ItemSelected(
                        isSelected = priority == 1,
                        item = ItemUi(
                            id = priority.toLong(),
                            title = UiText.DynamicString(if (priority == 1) stringResource(Res.string.priority_high) else stringResource(Res.string.priority_normal))
                        ),
                        onToggle = { priority = if (priority == 1) 0 else 1 }
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    IconButton(onClick = { showDatePicker.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    JalaliDatePickerBottomSheet(
        openSheet = showDatePicker,
        onConfirm = { calendar ->
            reminderTime = calendar.toTimestamp()
        }
    )
}
