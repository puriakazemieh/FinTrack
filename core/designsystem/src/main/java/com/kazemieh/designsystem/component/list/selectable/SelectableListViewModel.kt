package com.kazemieh.designsystem.component.list.selectable


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.ItemUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SelectableListViewModel : ViewModel() {

    private val _state = MutableStateFlow(SelectableState())
    val state: StateFlow<SelectableState> = _state.asStateFlow()

    private val _oneShot = MutableSharedFlow<SelectableOneShot>(replay = 0)
    val oneShot: SharedFlow<SelectableOneShot> = _oneShot.asSharedFlow()

    fun onIntent(intent: SelectableIntent) {
        when (intent) {
            is SelectableIntent.Load -> handleLoad(
                intent.items,
                intent.initialSelection,
                intent.showSelectAll
            )

            is SelectableIntent.Toggle -> toggle(intent.itemUi)
            SelectableIntent.ToggleSelectAll -> toggleSelectAll()
            is SelectableIntent.SetSelection -> setSelection(intent.items)
            SelectableIntent.Confirm -> viewModelScope.launch {
                _oneShot.emit(
                    SelectableOneShot.Confirmed(
                        state.value.selectedItems,
                        state.value.isAllSelected
                    )
                )
            }

            SelectableIntent.Dismiss -> viewModelScope.launch { _oneShot.emit(SelectableOneShot.Dismissed) }
            SelectableIntent.AddClick -> viewModelScope.launch { _oneShot.emit(SelectableOneShot.AddClick) }
        }
    }

    private fun handleLoad(
        items: Set<ItemUi>,
        initialSelection: Set<ItemUi>,
        showSelectAll: Boolean
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val selected =
                if (initialSelection.isEmpty() && showSelectAll) items.toSet()
                else initialSelection.intersect(items.toSet())
            val allSelected = items.isNotEmpty() && selected == items

            _state.update {
                SelectableState(
                    items = items,
                    selectedItems = selected,
                    isAllSelected = allSelected,
                    isLoading = false
                )
            }
        }
    }

    private fun toggle(itemUi: ItemUi) {
        _state.update {
            val current = it.selectedItems
            val next = if (current.contains(itemUi)) current - itemUi else current + itemUi
            val allSelected = it.items.isNotEmpty() && next == it.items
            it.copy(selectedItems = next, isAllSelected = allSelected)
        }
    }

    private fun toggleSelectAll() {
        _state.update {
            if (it.isAllSelected) it.copy(selectedItems = emptySet(), isAllSelected = false)
            else it.copy(selectedItems = it.items.toSet(), isAllSelected = true)
        }
    }

    private fun setSelection(items: Set<ItemUi>) {
        _state.update {
            val normalized = items.intersect(it.items)
            it.copy(
                selectedItems = normalized,
                isAllSelected = it.items.isNotEmpty() && normalized == it.items
            )
        }
    }
}

sealed interface SelectableOneShot {
    data class Confirmed(val selectedItems: Set<ItemUi>, val isAllSelected: Boolean) :
        SelectableOneShot

    object Dismissed : SelectableOneShot
    object AddClick : SelectableOneShot
}


sealed interface SelectableIntent {
    data class Load(
        val items: Set<ItemUi>,
        val initialSelection: Set<ItemUi> = emptySet(),
        val showSelectAll: Boolean = true
    ) : SelectableIntent

    data class Toggle(val itemUi: ItemUi) : SelectableIntent
    object ToggleSelectAll : SelectableIntent
    data class SetSelection(val items: Set<ItemUi>) : SelectableIntent
    object Confirm : SelectableIntent
    object Dismiss : SelectableIntent
    object AddClick : SelectableIntent
}


data class SelectableState(
    val items: Set<ItemUi> = emptySet(),
    val selectedItems: Set<ItemUi> = emptySet(),
    val isAllSelected: Boolean = false,
    val isLoading: Boolean = false
)

