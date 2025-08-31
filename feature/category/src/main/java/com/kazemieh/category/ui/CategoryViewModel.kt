package com.kazemieh.category.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllCategory
import com.kazemieh.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

class CategoryViewModel(
    private val getAllCategory: GetAllCategory
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllCategory().collect { categories ->
                _state.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }
}
