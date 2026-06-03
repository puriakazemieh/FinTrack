package com.kazemieh.category.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import kotlinx.coroutines.channels.Channel
import com.kazemieh.designsystem.component.glass.EntitySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CategoryViewModel(
    private val observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<CategoryEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CategoryIntent) {
        when (intent) {
            is CategoryIntent.LoadCategoryByType -> loadAllCategory(intent.type)

            is CategoryIntent.SetQuery -> _state.update { it.copy(query = intent.query) }

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

            is CategoryIntent.OnDeleteClick -> _state.update {
                it.copy(
                    isDeleteShow = !_state.value.isDeleteShow,
                    selectedCategory = intent.category
                )
            }

            is CategoryIntent.OnEditClick -> _state.update {
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
        _state.update { it.copy(type = type, query = "") }
        viewModelScope.launch {
            val baseFlow = if (type == TransactionType.ALL) {
                combine(
                    observeCategoriesUseCase(TransactionType.INCOME),
                    observeCategoriesUseCase(TransactionType.EXPENSE),
                    observeCategoriesUseCase(TransactionType.TRANSFER),
                ) { income, expense, transfer ->
                    val all = income + expense + transfer
                    updateSummary(income.size, expense.size, transfer.size)
                    all
                }
            } else {
                observeCategoriesUseCase(type).map { categories ->
                    val incomeCount = if (type == TransactionType.INCOME) categories.size else 0
                    val expenseCount = if (type == TransactionType.EXPENSE) categories.size else 0
                    val transferCount = if (type == TransactionType.TRANSFER) categories.size else 0
                    updateSummary(incomeCount, expenseCount, transferCount)
                    categories
                }
            }

            combine(
                baseFlow,
                _state.map { it.query }.distinctUntilChanged()
            ) { categories, query ->
                val filtered = if (query.isBlank()) categories else {
                    categories.filter { it.name.contains(query, ignoreCase = true) }
                }
                _state.update {
                    it.copy(
                        categories = filtered,
                        items = filtered.map { it.toItemUi() }.toSet()
                    )
                }
            }.stateIn(viewModelScope)
        }
    }

    private fun updateSummary(income: Int, expense: Int, transfer: Int) {
        val summaries = listOf(
            EntitySummary("درآمد", income.toString()),
            EntitySummary("هزینه", expense.toString()),
            EntitySummary("انتقال", transfer.toString())
        )
        _state.update { it.copy(summaries = summaries) }
    }
}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val items: Set<ItemUi> = emptySet(),
    val summaries: List<EntitySummary> = emptyList(),
    val query: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val listTransactionType: List<TransactionType> = listOf(
        TransactionType.INCOME,
        TransactionType.EXPENSE,
        TransactionType.TRANSFER
    ),
    val isAddShow: Boolean = false,
    val isDeleteShow: Boolean = false
)

sealed interface CategoryIntent {
    data class LoadCategoryByType(val type: TransactionType = TransactionType.INCOME) :
        CategoryIntent

    data class SetQuery(val query: String) : CategoryIntent

    data class SelectedCategory(val selectedCategory: Category) : CategoryIntent

    data object OnAddCategoryClick : CategoryIntent
    data class OnDeleteClick(val category: Category? = null) : CategoryIntent
    data class OnEditClick(val category: Category? = null) : CategoryIntent
    data object OnDismiss : CategoryIntent

}

sealed interface CategoryEffect {
    data class AddedCategory(val category: Category) : CategoryEffect
    data object OnDismiss : CategoryEffect
}