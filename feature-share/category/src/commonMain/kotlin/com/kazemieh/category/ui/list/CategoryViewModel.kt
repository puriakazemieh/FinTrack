package com.kazemieh.category.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.domain.usecase.ObserveCategoriesFlatUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.UpdateCategoryPositionsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CategoryViewModel(
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeCategoriesFlatUseCase: ObserveCategoriesFlatUseCase,
    private val updateCategoryPositionsUseCase: UpdateCategoryPositionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<CategoryEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CategoryIntent) {
        when (intent) {
            is CategoryIntent.LoadCategoryByType -> loadAllCategory(
                intent.type,
                intent.parentId,
                intent.isHierarchical
            )

            is CategoryIntent.OnParentCategoryClick -> {
                _state.update {
                    it.copy(
                        parentId = intent.parentCategory?.id,
                        parentCategory = intent.parentCategory
                    )
                }
                loadAllCategory(_state.value.type, intent.parentCategory?.id)
            }

            is CategoryIntent.ToggleExpand -> {
                _state.update {
                    val newExpanded = it.expandedCategoryIds.toMutableSet()
                    if (newExpanded.contains(intent.categoryId)) {
                        newExpanded.remove(intent.categoryId)
                    } else {
                        newExpanded.add(intent.categoryId)
                    }
                    it.copy(expandedCategoryIds = newExpanded)
                }
            }

            is CategoryIntent.SetQuery -> _state.update { it.copy(query = intent.query) }

            is CategoryIntent.SelectedCategory -> {
                viewModelScope.launch {
                    _effect.send(CategoryEffect.AddedCategory(intent.selectedCategory))
                    _state.update {
                        it.copy(
                            isAddShow = false,
                            selectedCategory = null,
                            isDeleteShow = false
                        )
                    }
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
                }
            }

            CategoryIntent.ResetFlags -> {
                _state.update {
                    it.copy(
                        isAddShow = false,
                        isDeleteShow = false,
                        selectedCategory = null,
                        isReorderShow = false
                    )
                }
            }

            CategoryIntent.OnToggleReorder -> {
                _state.update { it.copy(isReorderShow = !it.isReorderShow) }
            }

            is CategoryIntent.UpdatePositions -> {
                viewModelScope.launch {
                    updateCategoryPositionsUseCase(intent.positions)
                }
            }
        }

    }


    private fun loadAllCategory(
        type: TransactionType,
        parentId: Long? = null,
        isHierarchical: Boolean = false
    ) {
        _state.update { it.copy(type = type, query = "", parentId = parentId) }
        viewModelScope.launch {
            val baseFlow = if (isHierarchical) {
                observeCategoriesFlatUseCase(type).map { all ->
                    _state.update { it.copy(allCategories = all) }
                    all
                }
            } else if (type == TransactionType.ALL) {
                combine(
                    observeCategoriesUseCase(TransactionType.INCOME, parentId),
                    observeCategoriesUseCase(TransactionType.EXPENSE, parentId),
                    observeCategoriesUseCase(TransactionType.TRANSFER, parentId),
                ) { income, expense, transfer ->
                    val all = income + expense + transfer
                    updateSummary(income.size, expense.size, transfer.size)
                    all
                }
            } else if (state.value.query.isNotBlank()) {
                // If searching, show all categories (flat)
                observeCategoriesFlatUseCase(type).map { categories ->
                    categories.filter { it.name.contains(state.value.query, ignoreCase = true) }
                }
            } else {
                observeCategoriesUseCase(type, parentId).map { categories ->
                    val incomeCount = if (type == TransactionType.INCOME) categories.size else 0
                    val expenseCount = if (type == TransactionType.EXPENSE) categories.size else 0
                    val transferCount = if (type == TransactionType.TRANSFER) categories.size else 0
                    updateSummary(incomeCount, expenseCount, transferCount)
                    categories
                }
            }

            combine(
                baseFlow,
                _state.map { it.query }.distinctUntilChanged(),
                _state.map { it.expandedCategoryIds }.distinctUntilChanged()
            ) { categories, query, expandedIds ->
                val finalCategories = if (isHierarchical && query.isBlank()) {
                    val result = mutableListOf<Category>()
                    categories.filter { it.parentId == null }.forEach { parent ->
                        result.add(parent)
                        if (expandedIds.contains(parent.id)) {
                            result.addAll(categories.filter { it.parentId == parent.id })
                        }
                    }
                    result
                } else if (query.isBlank()) {
                    categories
                } else {
                    categories.filter { it.name.contains(query, ignoreCase = true) }
                }

                _state.update {
                    it.copy(
                        categories = finalCategories,
                        items = finalCategories.map { it.toItemUi() }.toSet()
                    )
                }
            }.stateIn(viewModelScope)
        }
    }

    private fun updateSummary(income: Int, expense: Int, transfer: Int) {
        val summaries = listOf(
            EntitySummary(UiText.StringResourceText(Res.string.type_income), income.toString()),
            EntitySummary(UiText.StringResourceText(Res.string.type_expense), expense.toString()),
            EntitySummary(UiText.StringResourceText(Res.string.type_transfer), transfer.toString())
        )
        _state.update { it.copy(summaries = summaries) }
    }
}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val allCategories: List<Category> = emptyList(), // Store all for hierarchical view
    val expandedCategoryIds: Set<Long> = emptySet(),
    val selectedCategory: Category? = null,
    val items: Set<ItemUi> = emptySet(),
    val summaries: List<EntitySummary> = emptyList(),
    val query: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val parentId: Long? = null,
    val parentCategory: Category? = null,
    val listTransactionType: List<TransactionType> = listOf(
        TransactionType.INCOME,
        TransactionType.EXPENSE,
        TransactionType.TRANSFER
    ),
    val isAddShow: Boolean = false,
    val isDeleteShow: Boolean = false,
    val isReorderShow: Boolean = false
)

sealed interface CategoryIntent {
    data class LoadCategoryByType(
        val type: TransactionType = TransactionType.INCOME,
        val parentId: Long? = null,
        val isHierarchical: Boolean = false
    ) :
        CategoryIntent

    data class SetQuery(val query: String) : CategoryIntent

    data class SelectedCategory(val selectedCategory: Category) : CategoryIntent
    data class OnParentCategoryClick(val parentCategory: Category?) : CategoryIntent
    data class ToggleExpand(val categoryId: Long) : CategoryIntent

    data object OnAddCategoryClick : CategoryIntent
    data class OnDeleteClick(val category: Category? = null) : CategoryIntent
    data class OnEditClick(val category: Category? = null) : CategoryIntent
    data object OnDismiss : CategoryIntent
    data object ResetFlags : CategoryIntent
    data object OnToggleReorder : CategoryIntent
    data class UpdatePositions(val positions: Map<Long, Int>) : CategoryIntent

}

sealed interface CategoryEffect {
    data class AddedCategory(val category: Category) : CategoryEffect
    data object OnDismiss : CategoryEffect
}