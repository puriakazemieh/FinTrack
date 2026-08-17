package com.kazemieh.check.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.usecase.CheckUseCaseGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckListViewModel(
    private val checkUseCases: CheckUseCaseGroup
) : ViewModel() {
    private val _state = MutableStateFlow(CheckListState())
    val state: StateFlow<CheckListState> = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val filterCategories = MutableStateFlow<Set<Category>>(emptySet())
    private val filterSources = MutableStateFlow<Set<Source>>(emptySet())
    private val filterTags = MutableStateFlow<Set<Tag>>(emptySet())
    private val filterPersons = MutableStateFlow<Set<Person>>(emptySet())

    init {
        observeChecks()
    }

    fun onIntent(intent: CheckListIntent) {
        when (intent) {
            is CheckListIntent.UpdateSearchQuery -> searchQuery.value = intent.query
            CheckListIntent.OpenFilters -> _state.update { it.copy(showFilterSheet = true) }
            CheckListIntent.DismissFilters -> _state.update { it.copy(showFilterSheet = false) }
            CheckListIntent.ResetFilters -> {
                filterCategories.value = emptySet()
                filterSources.value = emptySet()
                filterTags.value = emptySet()
                filterPersons.value = emptySet()
            }
            is CheckListIntent.UpdateFilters -> {
                filterCategories.value = intent.categories
                filterSources.value = intent.sources
                filterTags.value = intent.tags
                filterPersons.value = intent.persons
            }
            is CheckListIntent.UpdateStatus -> updateStatus(intent.checkId, intent.newStatus)
            is CheckListIntent.DeleteCheck -> deleteCheck(intent.checkId)
        }
    }

    private fun observeChecks() {
        viewModelScope.launch {
            combine(
                checkUseCases.observeAllChecksUseCase(),
                searchQuery,
                filterCategories,
                filterSources,
                filterTags,
                filterPersons
            ) { values ->
                val checks = values[0] as List<Check>
                val query = values[1] as String
                val categories = values[2] as Set<Category>
                val sources = values[3] as Set<Source>
                val tags = values[4] as Set<Tag>
                val persons = values[5] as Set<Person>
                val filtered = checks.filter { check ->
                    val matchesSearch = query.isBlank() ||
                        check.personName?.contains(query, ignoreCase = true) == true ||
                        check.description?.contains(query, ignoreCase = true) == true
                    val matchesCategory = categories.isEmpty() || check.categoryId in categories.mapNotNull { it.id }.toSet()
                    val matchesSource = sources.isEmpty() || check.sourceId in sources.mapNotNull { it.id }.toSet()
                    val matchesTags = tags.isEmpty() || check.tagIds.orEmpty().any { it in tags.mapNotNull { tag -> tag.id }.toSet() }
                    val matchesPerson = persons.isEmpty() || check.personId in persons.mapNotNull { it.id }.toSet()
                    matchesSearch && matchesCategory && matchesSource && matchesTags && matchesPerson
                }
                CheckResult(checks, filtered, categories, sources, tags, persons)
            }.collect { result ->
                _state.update {
                    it.copy(
                        checks = result.all,
                        filteredChecks = result.filtered,
                        searchQuery = searchQuery.value,
                        filterCategories = result.categories,
                        filterSources = result.sources,
                        filterTags = result.tags,
                        filterPersons = result.persons
                    )
                }
            }
        }
    }

    private fun updateStatus(checkId: Long, newStatus: CheckStatus) {
        viewModelScope.launch {
            _state.value.checks.find { it.id == checkId }?.let { check ->
                checkUseCases.updateCheckUseCase(check.copy(status = newStatus))
            }
        }
    }

    private fun deleteCheck(checkId: Long) {
        viewModelScope.launch { checkUseCases.deleteCheckUseCase(checkId) }
    }

    private data class CheckResult(
        val all: List<Check>,
        val filtered: List<Check>,
        val categories: Set<Category>,
        val sources: Set<Source>,
        val tags: Set<Tag>,
        val persons: Set<Person>
    )
}

data class CheckListState(
    val checks: List<Check> = emptyList(),
    val filteredChecks: List<Check> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showFilterSheet: Boolean = false,
    val filterCategories: Set<Category> = emptySet(),
    val filterSources: Set<Source> = emptySet(),
    val filterTags: Set<Tag> = emptySet(),
    val filterPersons: Set<Person> = emptySet()
)

sealed interface CheckListIntent {
    data class UpdateSearchQuery(val query: String) : CheckListIntent
    data object OpenFilters : CheckListIntent
    data object DismissFilters : CheckListIntent
    data object ResetFilters : CheckListIntent
    data class UpdateFilters(
        val categories: Set<Category>,
        val sources: Set<Source>,
        val tags: Set<Tag>,
        val persons: Set<Person>
    ) : CheckListIntent
    data class UpdateStatus(val checkId: Long, val newStatus: CheckStatus) : CheckListIntent
    data class DeleteCheck(val checkId: Long) : CheckListIntent
}
