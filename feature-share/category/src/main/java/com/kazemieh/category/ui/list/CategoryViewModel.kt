package com.kazemieh.category.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.ItemUi
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.toItemUi
import com.kazemieh.domain.usecase.GetAllCategoryByType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CategoryViewModel(
    private val getAllCategoryByType: GetAllCategoryByType
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<CategoryEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CategoryIntent) {
        when (intent) {
            is CategoryIntent.LoadCategoryByType -> loadAllCategory(intent.type)

            is CategoryIntent.SelectedCategory -> {
                viewModelScope.launch {
                    _effect.send(CategoryEffect.AddedCategory(intent.selectedCategory))
                    _state.update { CategoryState() }
                }
            }

            CategoryIntent.OnAddCategoryClick -> _state.update {
                it.copy(
                    isAddShow = !_state.value.isAddShow,
                    selectedCategory = null,
                    isDeleteShow = false
                )
            }

            is CategoryIntent.OnDeleteCategoryClick -> _state.update {
                it.copy(
                    isDeleteShow = !_state.value.isDeleteShow,
                    selectedCategory = intent.category
                )
            }

            is CategoryIntent.OnEditCategoryClick -> _state.update {
                it.copy(
                    isAddShow = true,
                    selectedCategory = intent.category
                )
            }

            CategoryIntent.OnDismiss -> {
                viewModelScope.launch {
                    _effect.send(CategoryEffect.OnDismiss)
                    _state.update { CategoryState() }
                }
            }
        }

    }


    private fun loadAllCategory(type: TransactionType) {
        _state.update { it.copy(type = type) }
        viewModelScope.launch {
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
                            items = allCategory.map { it.toItemUi() }.toSet()
                        )
                    }
                }.stateIn(viewModelScope)
            } else {
                getAllCategoryByType(type).collect { categories ->
                    _state.update {
                        it.copy(
                            categories = categories,
                            items = categories.map { it.toItemUi() }.toSet()
                        )
                    }
                }
            }

        }
    }

}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val items: Set<ItemUi> = emptySet(),
    val type: TransactionType = TransactionType.INCOME,
    val listTransactionType: List<TransactionType> = listOf(
        TransactionType.INCOME,
        TransactionType.EXPENSE
    ),
    val isAddShow: Boolean = false,
    val isDeleteShow: Boolean = false
)

sealed interface CategoryIntent {
    data class LoadCategoryByType(val type: TransactionType = TransactionType.INCOME) :
        CategoryIntent

    data class SelectedCategory(val selectedCategory: Category) : CategoryIntent

    data object OnAddCategoryClick : CategoryIntent
    data class OnDeleteCategoryClick(val category: Category? = null) : CategoryIntent
    data class OnEditCategoryClick(val category: Category? = null) : CategoryIntent
    data object OnDismiss : CategoryIntent

}

sealed interface CategoryEffect {
    data class AddedCategory(val category: Category) : CategoryEffect
    data object OnDismiss : CategoryEffect
}