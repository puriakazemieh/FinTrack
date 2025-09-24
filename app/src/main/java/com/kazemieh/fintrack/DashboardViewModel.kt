package com.kazemieh.fintrack

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class DashboardViewModel() : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state


    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.SetSource -> {
                _state.update {
                    it.copy(
                        setSource = intent.setSource,
                        showSourceList = false,
                        showAddSource = false
                    )
                }
            }

            is DashboardIntent.SetCategory -> {
                _state.update {
                    it.copy(
                        setCategory = intent.setCategory,
                        showCategoryList = false,
                        showAddCategory = false
                    )
                }
            }


            is DashboardIntent.ShowAddSource -> {
                _state.update {
                    it.copy(
                        showAddSource = intent.showAddSource,
                        fromSourceList = intent.fromSourceList,
                        setSource = if (!intent.showAddSource && intent.fromSourceList) null else it.setSource
                    )
                }
            }

            is DashboardIntent.ShowAddCategory -> {
                _state.update {
                    it.copy(
                        showAddCategory = intent.showAddCategory,
                        setSource = if (!intent.showAddCategory) null else it.setCategory
                    )
                }
            }


            is DashboardIntent.ShowAddTransaction -> {
                _state.update {
                    it.copy(
                        showAddTransaction = intent.showAddTransaction,
                        setSource = if (!intent.showAddTransaction) null else it.setSource
                    )
                }
            }

            is DashboardIntent.ShowSourceList -> _state.update { it.copy(showSourceList = intent.sourceList) }
            is DashboardIntent.ShowCategoryList -> _state.update { it.copy(showCategoryList = intent.categoryList) }
        }
    }


}

data class DashboardState(
    val setSource: Pair<Int, String>? = null,
    val setCategory: Pair<Int, String>? = null,
    val showSourceList: Boolean = false,
    val showCategoryList: Boolean = false,
    val showAddSource: Boolean = false,
    val showAddCategory: Boolean = false,
    val showAddTransaction: Boolean = false,
    val fromSourceList: Boolean = false,
)


sealed interface DashboardIntent {
    data class ShowAddTransaction(val showAddTransaction: Boolean = false) : DashboardIntent

    data class ShowSourceList(val sourceList: Boolean = false) : DashboardIntent
    data class ShowCategoryList(val categoryList: Boolean = false) : DashboardIntent

    data class ShowAddSource(
        val showAddSource: Boolean = false,
        val fromSourceList: Boolean = false
    ) : DashboardIntent

    data class ShowAddCategory(val showAddCategory: Boolean = false) : DashboardIntent

    data class SetSource(val setSource: Pair<Int, String>? = null) : DashboardIntent
    data class SetCategory(val setCategory: Pair<Int, String>? = null) : DashboardIntent

}
