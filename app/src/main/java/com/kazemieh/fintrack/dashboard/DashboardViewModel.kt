package com.kazemieh.fintrack.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class DashboardViewModel() : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state


    fun onIntent(intent: DashboardIntent) {
        when (intent) {

            is DashboardIntent.ShowAddTransaction -> {
                _state.update {
                    it.copy(
                        showAddTransaction = intent.showAddTransaction,
                        source = if (!intent.showAddTransaction) null else it.source,
                        sourceEnd = if (!intent.showAddTransaction) null else it.sourceEnd,
                        category = if (!intent.showAddTransaction) null else it.category,
                        tags = if (!intent.showAddTransaction) null else it.tags,
                        persons = if (!intent.showAddTransaction) null else it.persons,
                    )
                }
            }


            is DashboardIntent.SetSource -> {
                _state.update {
                    it.copy(
                        source = if (!state.value.isSourceEnd) intent.source else state.value.source,
                        sourceEnd = if (state.value.isSourceEnd) intent.source else state.value.sourceEnd,
                        showSourceList = false,
                        showAddSource = false,
                        isSourceEnd = false
                    )
                }
            }

            is DashboardIntent.ShowAddSource -> {
                _state.update {
                    it.copy(
                        showAddSource = intent.showAddSource,
                        fromSourceList = intent.fromSourceList,
                        source = if (!intent.showAddSource && intent.fromSourceList) null else it.source,
                        sourceEnd = if (!intent.showAddSource && intent.fromSourceList) null else it.sourceEnd
                    )
                }
            }

            is DashboardIntent.ShowSourceList -> _state.update {
                it.copy(
                    showSourceList = intent.sourceList,
                    isSourceEnd = intent.isSourceEnd
                )
            }


            is DashboardIntent.SetCategory -> {
                _state.update {
                    it.copy(
                        category = intent.category,
                        showCategoryList = false,
                        showAddCategory = false
                    )
                }
            }

            is DashboardIntent.ShowAddCategory -> {
                _state.update {
                    it.copy(
                        showAddCategory = intent.showAddCategory,
                        source = if (!intent.showAddCategory) null else it.category
                    )
                }
            }

            is DashboardIntent.ShowCategoryList -> _state.update {
                it.copy(
                    showCategoryList = intent.categoryList,
                    selectedTransactionType = intent.selectedTransactionType,
                    category = if (intent.selectedTransactionType != it.selectedTransactionType) null else it.category
                )
            }


            is DashboardIntent.ShowTagList -> _state.update { it.copy(showTagList = intent.showTagList) }

            is DashboardIntent.SetAllSelectedTags -> _state.update {
                it.copy(
                    tags = intent.tags,
                    showTagList = false
                )
            }


            is DashboardIntent.ShowPersonList -> _state.update { it.copy(showPersonList = intent.showPersonList) }

            is DashboardIntent.SetAllSelectedPerson -> _state.update {
                it.copy(
                    persons = intent.persons,
                    showPersonList = false
                )
            }
        }
    }


}

data class DashboardState(
    val showAddTransaction: Boolean = false,

    val source: Pair<Int, String>? = null,
    val sourceEnd: Pair<Int, String>? = null,
    val showSourceList: Boolean = false,
    val showAddSource: Boolean = false,
    val fromSourceList: Boolean = false,

    val category: Pair<Int, String>? = null,
    val showCategoryList: Boolean = false,
    val showAddCategory: Boolean = false,

    val tags: Set<Pair<Int, String>>? = null,
    val showTagList: Boolean = false,

    val persons: Set<Pair<Int, String>>? = null,
    val showPersonList: Boolean = false,

    val isSourceEnd: Boolean = false,

    val selectedTransactionType: Int = 1,
)


sealed interface DashboardIntent {
    data class ShowAddTransaction(val showAddTransaction: Boolean = false) : DashboardIntent

    data class SetSource(val source: Pair<Int, String>? = null) : DashboardIntent
    data class ShowSourceList(val sourceList: Boolean = false, val isSourceEnd: Boolean = false) :
        DashboardIntent

    data class ShowAddSource(
        val showAddSource: Boolean = false,
        val fromSourceList: Boolean = false
    ) : DashboardIntent


    data class ShowAddCategory(val showAddCategory: Boolean = false) : DashboardIntent
    data class SetCategory(val category: Pair<Int, String>? = null) : DashboardIntent
    data class ShowCategoryList(
        val categoryList: Boolean = false,
        val selectedTransactionType: Int = 1
    ) : DashboardIntent

    data class ShowTagList(val showTagList: Boolean = false) : DashboardIntent
    data class SetAllSelectedTags(val tags: Set<Pair<Int, String>>? = null) : DashboardIntent

    data class ShowPersonList(val showPersonList: Boolean = false) : DashboardIntent
    data class SetAllSelectedPerson(val persons: Set<Pair<Int, String>>? = null) : DashboardIntent


}
