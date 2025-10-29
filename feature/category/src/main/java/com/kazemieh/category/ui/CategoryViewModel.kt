package com.kazemieh.category.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllCategoryByType
import com.kazemieh.model.Category
import com.kazemieh.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
            is CategoryIntent.SelectedCategory -> {
                val current = _state.value.selectedCategory ?: emptySet()
                val selectedCategory =
                    if (current.contains(intent.selectedCategory))
                        current - intent.selectedCategory
                    else
                        current + intent.selectedCategory

                _state.update {
                    it.copy(selectedCategory = selectedCategory)
                }
            }
        }

    }


    private fun loadAllCategory(type: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (type == 0) {
                combine(
                    getAllCategoryByType(TransactionType.INCOME.count),
                    getAllCategoryByType(TransactionType.EXPENSE.count)
                ) { categoryIncome, categoryExpense ->
                    _state.update {
                        it.copy(
                            categories = categoryIncome + categoryExpense,
                            isLoading = false
                        )
                    }
                }.stateIn(viewModelScope)
            } else {
                getAllCategoryByType(type).collect { categories ->
                    _state.update { it.copy(categories = categories, isLoading = false) }
                }
            }

        }
    }
}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Set<Pair<Int, String>>? = null,
    val isLoading: Boolean = false
)

sealed interface CategoryIntent {
    data class LoadCategoryByType(val type: Int) : CategoryIntent
    data class SelectedCategory(val selectedCategory: Pair<Int, String>) : CategoryIntent

}
