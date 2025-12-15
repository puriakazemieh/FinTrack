package com.kazemieh.category.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.ItemUi
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.toItemUi
import com.kazemieh.domain.usecase.GetAllCategoryByType
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
            is CategoryIntent.LoadCategoryByType -> loadAllCategory(intent.type, false)
            is CategoryIntent.SelectedCategory -> {
                val current = _state.value.selectedCategory
                val selectedCategory =
                    if (current.contains(intent.selectedCategory))
                        current - intent.selectedCategory
                    else
                        current + intent.selectedCategory

                val isAllCategorySelected =
                    allCategorySelect(_state.value.categories) == selectedCategory

                _state.update {
                    it.copy(
                        selectedCategory = selectedCategory,
                        isAllCategorySelected = isAllCategorySelected
                    )
                }
            }

            is CategoryIntent.SelectedAllCategory -> {
                if (state.value.isAllCategorySelected) {
                    _state.update {
                        it.copy(
                            selectedCategory = emptySet(),
                            isAllCategorySelected = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            selectedCategory = allCategorySelect(state.value.categories),
                            isAllCategorySelected = true
                        )
                    }
                }
            }

            is CategoryIntent.IsAllCategorySelected -> {
                loadAllCategory(
                    intent.selectedTransactionType,
                    isAllCategorySelected = intent.isAllCategorySelected
                )
            }

            is CategoryIntent.SelectedCategories -> _state.update {
                it.copy(
                    selectedCategory = intent.selectedCategory
                )
            }

            CategoryIntent.OnAddCategoryClick -> _state.update {
                it.copy(
                    isAddShow = !_state.value.isAddShow
                )
            }
        }

    }


    private fun loadAllCategory(type: TransactionType, isAllCategorySelected: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (type == TransactionType.ALL) {
                combine(
                    getAllCategoryByType(TransactionType.INCOME),
                    getAllCategoryByType(TransactionType.EXPENSE),
                    getAllCategoryByType(TransactionType.TRANSFER),
                ) { categoryIncome, categoryExpense, categoryTransfer ->
                    val allCategory = categoryIncome + categoryExpense + categoryTransfer
                    _state.update {
                        it.copy(
                            categories = allCategory,
                            items = allCategory.map { it.toItemUi() }.toSet(),
                            isLoading = false,
                            selectedCategory = if (isAllCategorySelected) allCategorySelect(
                                allCategory = allCategory
                            ) else state.value.selectedCategory
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

    private fun allCategorySelect(allCategory: List<Category>): Set<Pair<Int, String>> {
        return allCategory.map { category ->
            (category.id?.toInt() ?: 0) to category.name
        }.toSet()
    }
}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val items: Set<ItemUi> = emptySet(),
    val selectedCategory: Set<Pair<Int, String>> = emptySet(),
    val isLoading: Boolean = false,
    val isAddShow: Boolean = false,
    val isAllCategorySelected: Boolean = true
)

sealed interface CategoryIntent {
    data class LoadCategoryByType(val type: TransactionType) : CategoryIntent
    data class SelectedCategory(val selectedCategory: Pair<Int, String>) : CategoryIntent
    data class SelectedCategories(val selectedCategory: Set<Pair<Int, String>>) : CategoryIntent
    data object SelectedAllCategory : CategoryIntent
    data class IsAllCategorySelected(
        val isAllCategorySelected: Boolean = true,
        val selectedTransactionType: TransactionType
    ) : CategoryIntent

    data object OnAddCategoryClick : CategoryIntent

}
