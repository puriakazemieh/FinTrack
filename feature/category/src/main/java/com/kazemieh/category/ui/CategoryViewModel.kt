package com.kazemieh.category.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllCategoryByType
import com.kazemieh.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CategoryViewModel(
    private val getAllCategoryByType: GetAllCategoryByType
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()


    fun onIntent(intent: CategoryIntent) {
        when (intent) {
            is CategoryIntent.LoadCategoryByType -> loadAllCategory(intent.type)
        }

    }


   private fun loadAllCategory(type: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllCategoryByType(type).collect { categories ->
                _state.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }
}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

sealed class CategoryIntent {
    data class LoadCategoryByType(val type: Int) : CategoryIntent()

}
