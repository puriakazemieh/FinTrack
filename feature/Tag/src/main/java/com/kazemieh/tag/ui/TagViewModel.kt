package com.kazemieh.tag.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllTag
import com.kazemieh.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TagState(
    val tag: List<Tag> = emptyList(),
    val isLoading: Boolean = false
)

class TagViewModel(
    private val getAllTag: GetAllTag
) : ViewModel() {

    private val _state = MutableStateFlow(TagState())
    val state = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllTag().collect { tag ->
                _state.update { it.copy(tag = tag, isLoading = false) }
            }
        }
    }
}
