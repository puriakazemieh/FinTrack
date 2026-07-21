package com.kazemieh.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.usecase.AddShoppingItemUseCase
import com.kazemieh.domain.usecase.DeleteShoppingItemUseCase
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedShoppingTagsUseCase
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

data class ShoppingState(
    val items: List<ShoppingItem> = emptyList(),
    val filteredItems: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showAddSheet: Boolean = false,
    val editingItem: ShoppingItem? = null,
    val editingCategory: Category? = null,
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedTags: List<Tag> = emptyList(),
    val showPurchased: Boolean = true,
    val filterCategories: Set<Category> = emptySet(),
    val filterTags: Set<Tag> = emptySet(),
    val itemToDelete: Long? = null,
    val showFilterSheet: Boolean = false
)

sealed interface ShoppingIntent {
    data class UpdateSearchQuery(val query: String) : ShoppingIntent
    data object OnAddClick : ShoppingIntent
    data object OnAddSheetDismiss : ShoppingIntent
    data class OnSaveNewItem(val item: ShoppingItem) : ShoppingIntent
    data class OnSaveAndNext(val item: ShoppingItem) : ShoppingIntent
    data class OnEditItem(val item: ShoppingItem?) : ShoppingIntent
    data class OnUpdateItem(val item: ShoppingItem) : ShoppingIntent
    data class OnToggleItem(val item: ShoppingItem) : ShoppingIntent
    data class OnDeleteItem(val id: Long) : ShoppingIntent
    data class OnDeleteClick(val id: Long) : ShoppingIntent
    data object OnDeleteCancel : ShoppingIntent
    data class OnReorder(val from: Int, val to: Int) : ShoppingIntent
    data object OnTogglePurchasedVisibility : ShoppingIntent
    data class OnFilterUpdate(val categories: Set<Category>, val tags: Set<Tag>) : ShoppingIntent
    data object OnFilterReset : ShoppingIntent
    data object OnFilterClick : ShoppingIntent
    data object OnFilterSheetDismiss : ShoppingIntent
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
    private val getCategory: GetCategoryUseCase,
    private val observeMostUsedCategories: ObserveMostUsedCategoriesUseCase,
    private val observeMostUsedTags: ObserveMostUsedShoppingTagsUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ShoppingEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterCategories = MutableStateFlow<Set<Category>>(emptySet())
    private val _filterTags = MutableStateFlow<Set<Tag>>(emptySet())

    init {
        _state.update { it.copy(isLoading = true) }
        observeMostUsedCategories(TransactionType.EXPENSE, limit = 3)
            .onEach { categories -> _state.update { it.copy(mostUsedCategories = categories) } }
            .launchIn(viewModelScope)

        observeMostUsedTags(limit = 3)
            .onEach { tags -> _state.update { it.copy(mostUsedTags = tags) } }
            .launchIn(viewModelScope)

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        combine(_filterCategories, _filterTags) { categories, tags ->
            categories to tags
        }.flatMapLatest { (categories, tags) ->
            observeShoppingItems(categories.mapNotNull { it.id }, tags.mapNotNull { it.id })
        }.combine(_searchQuery) { items, query ->
            val filtered = items.filter {
                it.name.contains(query, ignoreCase = true)
            }
            items to filtered
        }.onEach { (all, filtered) ->
            _state.update {
                it.copy(
                    items = all,
                    filteredItems = filtered,
                    isLoading = false,
                    searchQuery = _searchQuery.value,
                    filterCategories = _filterCategories.value,
                    filterTags = _filterTags.value
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: ShoppingIntent) {
        when (intent) {
            is ShoppingIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            ShoppingIntent.OnAddClick -> _state.update { it.copy(showAddSheet = true) }
            ShoppingIntent.OnAddSheetDismiss -> _state.update { it.copy(showAddSheet = false) }
            is ShoppingIntent.OnSaveNewItem -> addItem(intent.item, dismiss = true)
            is ShoppingIntent.OnSaveAndNext -> addItem(intent.item, dismiss = false)
            is ShoppingIntent.OnEditItem -> openEditor(intent.item)
            is ShoppingIntent.OnUpdateItem -> updateItem(intent.item)
            is ShoppingIntent.OnToggleItem -> toggleItem(intent.item)
            is ShoppingIntent.OnDeleteItem -> {
                deleteItem(intent.id)
                _state.update { it.copy(itemToDelete = null) }
            }
            is ShoppingIntent.OnDeleteClick -> _state.update { it.copy(itemToDelete = intent.id) }
            ShoppingIntent.OnDeleteCancel -> _state.update { it.copy(itemToDelete = null) }
            is ShoppingIntent.OnReorder -> reorderItems(intent.from, intent.to)
            ShoppingIntent.OnTogglePurchasedVisibility -> _state.update { it.copy(showPurchased = !it.showPurchased) }
            is ShoppingIntent.OnFilterUpdate -> {
                _filterCategories.value = intent.categories
                _filterTags.value = intent.tags
            }
            ShoppingIntent.OnFilterReset -> {
                _filterCategories.value = emptySet()
                _filterTags.value = emptySet()
            }
            ShoppingIntent.OnFilterClick -> _state.update { it.copy(showFilterSheet = true) }
            ShoppingIntent.OnFilterSheetDismiss -> _state.update { it.copy(showFilterSheet = false) }
        }
    }

    private fun openEditor(item: ShoppingItem?) {
        if (item == null) {
            _state.update { it.copy(editingItem = null, editingCategory = null) }
            return
        }
        _state.update { it.copy(editingItem = item, editingCategory = null) }
        val categoryId = item.categoryId ?: return
        viewModelScope.launch {
            val category = getCategory(categoryId)
            _state.update { it.copy(editingCategory = category) }
        }
    }

    private fun addItem(item: ShoppingItem, dismiss: Boolean) {
        if (item.name.isBlank()) return
        viewModelScope.launch {
            val newItem = item.copy(
                position = (_state.value.items.maxOfOrNull { it.position } ?: -1) + 1
            )
            val id = addShoppingItem(newItem)
            scheduleReminder(id, newItem)
            if (dismiss) {
                _state.update { it.copy(showAddSheet = false) }
            }
        }
    }

    private fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            updateShoppingItem(item)
            scheduleReminder(item.id, item)
            _state.update { it.copy(editingItem = null, editingCategory = null) }
        }
    }

    private suspend fun scheduleReminder(id: Long, item: ShoppingItem) {
        val reminderTime = item.reminderTime ?: return
        notificationScheduler.scheduleReminder(
            id = "shopping_$id",
            title = getString(Res.string.shopping_list),
            message = item.name,
            scheduledTime = Instant.fromEpochMilliseconds(reminderTime)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            channelId = NotificationManager.CHANNEL_SHOPPING
        )
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
