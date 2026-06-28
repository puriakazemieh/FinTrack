package com.kazemieh.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.domain.usecase.AddShoppingItemUseCase
import com.kazemieh.domain.usecase.DeleteShoppingItemUseCase
import com.kazemieh.domain.usecase.ObserveShoppingItemsUseCase
import com.kazemieh.domain.usecase.UpdateShoppingItemUseCase
import com.kazemieh.domain.usecase.UpdateShoppingPositionsUseCase
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.NotificationManager
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.shopping_list
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

data class ShoppingState(
    val items: List<ShoppingItem> = emptyList(),
    val filteredItems: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val newItemName: String = "",
    val newItemEstimatedPrice: String = "",
    val newItemPriority: Int = 0,
    val newItemReminderTime: Long? = null,
    val editingItem: ShoppingItem? = null,
    val searchQuery: String = ""
)

sealed interface ShoppingIntent {
    data class UpdateSearchQuery(val query: String) : ShoppingIntent
    data class OnNewItemNameChanged(val name: String) : ShoppingIntent
    data class OnNewItemPriceChanged(val price: String) : ShoppingIntent
    data class OnNewItemPriorityChanged(val priority: Int) : ShoppingIntent
    data class OnNewItemReminderChanged(val time: Long?) : ShoppingIntent
    data object OnAddItem : ShoppingIntent
    data class OnEditItem(val item: ShoppingItem?) : ShoppingIntent
    data class OnUpdateItem(val item: ShoppingItem) : ShoppingIntent
    data class OnToggleItem(val item: ShoppingItem) : ShoppingIntent
    data class OnDeleteItem(val id: Long) : ShoppingIntent
    data class OnReorder(val from: Int, val to: Int) : ShoppingIntent
}

sealed interface ShoppingEffect {
    data class ShowError(val message: String) : ShoppingEffect
}

class ShoppingViewModel(
    private val observeShoppingItems: ObserveShoppingItemsUseCase,
    private val addShoppingItem: AddShoppingItemUseCase,
    private val updateShoppingItem: UpdateShoppingItemUseCase,
    private val deleteShoppingItem: DeleteShoppingItemUseCase,
    private val updatePositions: UpdateShoppingPositionsUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ShoppingEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        _state.update { it.copy(isLoading = true) }
        observeShoppingItems()
            .combine(_searchQuery) { items, query ->
                val filtered = items.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                items to filtered
            }
            .onEach { (all, filtered) ->
                _state.update {
                    it.copy(
                        items = all,
                        filteredItems = filtered,
                        isLoading = false,
                        searchQuery = _searchQuery.value
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: ShoppingIntent) {
        when (intent) {
            is ShoppingIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is ShoppingIntent.OnNewItemNameChanged -> {
                _state.update { it.copy(newItemName = intent.name) }
            }
            is ShoppingIntent.OnNewItemPriceChanged -> {
                _state.update { it.copy(newItemEstimatedPrice = intent.price) }
            }
            is ShoppingIntent.OnNewItemPriorityChanged -> {
                _state.update { it.copy(newItemPriority = intent.priority) }
            }
            is ShoppingIntent.OnNewItemReminderChanged -> {
                _state.update { it.copy(newItemReminderTime = intent.time) }
            }
            ShoppingIntent.OnAddItem -> addItem()
            is ShoppingIntent.OnEditItem -> {
                _state.update { it.copy(editingItem = intent.item) }
            }
            is ShoppingIntent.OnUpdateItem -> updateItem(intent.item)
            is ShoppingIntent.OnToggleItem -> toggleItem(intent.item)
            is ShoppingIntent.OnDeleteItem -> deleteItem(intent.id)
            is ShoppingIntent.OnReorder -> reorderItems(intent.from, intent.to)
        }
    }

    private fun addItem() {
        val name = _state.value.newItemName
        if (name.isBlank()) return

        viewModelScope.launch {
            val newItem = ShoppingItem(
                name = name,
                estimatedPrice = _state.value.newItemEstimatedPrice.toDoubleOrNull() ?: 0.0,
                priority = _state.value.newItemPriority,
                reminderTime = _state.value.newItemReminderTime,
                position = (_state.value.items.maxOfOrNull { it.position } ?: -1) + 1
            )
            val id = addShoppingItem(newItem)
            newItem.reminderTime?.let { reminderTime ->
                notificationScheduler.scheduleReminder(
                    id = "shopping_$id",
                    title = getString(Res.string.shopping_list),
                    message = newItem.name,
                    scheduledTime = Instant.fromEpochMilliseconds(reminderTime).toLocalDateTime(TimeZone.currentSystemDefault()),
                    channelId = NotificationManager.CHANNEL_SHOPPING
                )
            }
            _state.update { it.copy(newItemName = "", newItemEstimatedPrice = "", newItemPriority = 0, newItemReminderTime = null) }
        }
    }

    private fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            updateShoppingItem(item)
            item.reminderTime?.let { reminderTime ->
                notificationScheduler.scheduleReminder(
                    id = "shopping_${item.id}",
                    title = getString(Res.string.shopping_list),
                    message = item.name,
                    scheduledTime = Instant.fromEpochMilliseconds(reminderTime).toLocalDateTime(TimeZone.currentSystemDefault()),
                    channelId = NotificationManager.CHANNEL_SHOPPING
                )
            }
            _state.update { it.copy(editingItem = null) }
        }
    }

    private fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            updateShoppingItem(item.copy(isChecked = !item.isChecked))
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            deleteShoppingItem(id)
        }
    }

    private fun reorderItems(from: Int, to: Int) {
        val list = _state.value.items.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)

        val updatedList = list.mapIndexed { index, shoppingItem ->
            shoppingItem.copy(position = index)
        }

        _state.update { it.copy(items = updatedList) }

        viewModelScope.launch {
            updatePositions(updatedList)
        }
    }
}
