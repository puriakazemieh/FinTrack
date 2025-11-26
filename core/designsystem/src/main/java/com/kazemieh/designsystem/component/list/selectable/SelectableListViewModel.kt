package com.kazemieh.designsystem.component.list.selectable


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.component.list.ItemUi
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

            is SelectableIntent.Toggle -> toggle(intent.id)
            SelectableIntent.ToggleSelectAll -> toggleSelectAll()
            is SelectableIntent.SetSelection -> setSelection(intent.ids)
            SelectableIntent.Confirm -> viewModelScope.launch {
                _oneShot.emit(
                    SelectableOneShot.Confirmed(
                        state.value.selectedIds,
                        state.value.isAllSelected
                    )
                )
            }

            SelectableIntent.Dismiss -> viewModelScope.launch { _oneShot.emit(SelectableOneShot.Dismissed) }
            SelectableIntent.AddClick -> viewModelScope.launch { _oneShot.emit(SelectableOneShot.AddClick) }
        }
    }

    private fun handleLoad(
        items: List<ItemUi>,
        initialSelection: Set<Int>,
        showSelectAll: Boolean
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val allIds = items.map { it.id }.toSet()
            val selected =
                if (initialSelection.isEmpty() && showSelectAll) allIds
                else initialSelection.intersect(allIds)
            val allSelected = allIds.isNotEmpty() && selected == allIds

            _state.update {
                SelectableState(
                    items = items,
                    selectedIds = selected,
                    isAllSelected = allSelected,
                    isLoading = false
                )
            }
        }
    }

    private fun toggle(id: Int) {
        _state.update { s ->
            val current = s.selectedIds
            val next = if (current.contains(id)) current - id else current + id
            val allIds = s.items.map { it.id }.toSet()
            val allSelected = allIds.isNotEmpty() && next == allIds
            s.copy(selectedIds = next, isAllSelected = allSelected)
        }
    }

    private fun toggleSelectAll() {
        _state.update { s ->
            val allIds = s.items.map { it.id }.toSet()
            if (s.isAllSelected) s.copy(selectedIds = emptySet(), isAllSelected = false)
            else s.copy(selectedIds = allIds, isAllSelected = true)
        }
    }

    private fun setSelection(ids: Set<Int>) {
        _state.update { s ->
            val allIds = s.items.map { it.id }.toSet()
            val normalized = ids.intersect(allIds)
            s.copy(
                selectedIds = normalized,
                isAllSelected = allIds.isNotEmpty() && normalized == allIds
            )
        }
    }
}

sealed interface SelectableOneShot {
    data class Confirmed(val selectedId: Set<Int>, val isAllSelected: Boolean) : SelectableOneShot
    object Dismissed : SelectableOneShot
    object AddClick : SelectableOneShot
}


sealed interface SelectableIntent {
    data class Load(
        val items: List<ItemUi>,
        val initialSelection: Set<Int> = emptySet(),
        val showSelectAll: Boolean = true
    ) : SelectableIntent

    data class Toggle(val id: Int) : SelectableIntent
    object ToggleSelectAll : SelectableIntent
    data class SetSelection(val ids: Set<Int>) : SelectableIntent
    object Confirm : SelectableIntent
    object Dismiss : SelectableIntent
    object AddClick : SelectableIntent
}


data class SelectableState(
    val items: List<ItemUi> = emptyList(),
    val selectedIds: Set<Int> = emptySet(),
    val isAllSelected: Boolean = false,
    val isLoading: Boolean = false
)

