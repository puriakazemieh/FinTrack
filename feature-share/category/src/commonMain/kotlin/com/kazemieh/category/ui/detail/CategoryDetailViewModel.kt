package com.kazemieh.category.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.ProductEvent
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryDetailState(
    val category: Category? = null,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val isLoading: Boolean = false
)

class CategoryDetailViewModel(
    private val analytics: AnalyticsService,
    private val categoryId: Long,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val getCategoryUseCase: GetCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        analytics.track(ProductEvent.FeatureOpened("category_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val category = getCategoryUseCase(categoryId)
            _state.update { it.copy(category = category) }
        }

        viewModelScope.launch {
            observeTransactionsUseCase(
                TransactionFilterParams(
                    categories = setOf(Category(id = categoryId, name = "", colorId = 1, iconId = 1)),
                    isAllCategories = false
                ),
                PageRequest(limit = Int.MAX_VALUE, offset = 0)
            ).collect { page ->
                _state.update { it.copy(transactions = page.items, isLoading = false) }
            }
        }
    }
}
