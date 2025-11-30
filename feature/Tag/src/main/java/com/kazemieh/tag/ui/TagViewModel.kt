package com.kazemieh.tag.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.domain.usecase.GetAllTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagViewModel(
    private val getAllTag: GetAllTag
) : ViewModel() {

    private val _state = MutableStateFlow(TagState())
    val state = _state.asStateFlow()

    fun onIntent(intent: TagIntent) {
        when (intent) {
            TagIntent.GetAllTag -> loadAllTags()

            is TagIntent.SetSelectedTag -> {
                val current = _state.value.initialSelectionIds
                val person = intent.tag.first
                val selectedPersons =
                    if (current.contains(person))
                        current - person
                    else
                        current + person

                _state.update {
                    it.copy(initialSelectionIds = selectedPersons, showAddTag = false)
                }
            }

            is TagIntent.SetAllSelectedTags -> _state.update {
                it.copy(initialSelectionIds = intent.tags?.map { it.first }?.toSet() ?: emptySet())
            }

            is TagIntent.ShowAddTag -> _state.update {
                it.copy(showAddTag = !state.value.showAddTag)
            }

        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllTag().collect { tags ->
                _state.update {
                    it.copy(
                        tags = tags,
                        items = tags.map { ItemUi(it.id?.toInt() ?: 0, it.name) },
                        isLoading = false
                    )
                }
            }
        }
    }


}

data class TagState(
    val tags: List<Tag> = emptyList(),
    val showAddTag: Boolean = false,
    val isLoading: Boolean = false,
    val initialSelectionIds: Set<Int> = emptySet(),
    val items: List<ItemUi> = emptyList(),
)

sealed interface TagIntent {
    data class SetSelectedTag(val tag: Pair<Int, String>) : TagIntent
    data class SetAllSelectedTags(val tags: Set<Pair<Int, String>>? = null) : TagIntent
    data object GetAllTag : TagIntent
    data object ShowAddTag : TagIntent
}