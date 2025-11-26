package com.kazemieh.tag.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllTag
import com.kazemieh.common.model.Tag
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
                val current = _state.value.selectedTags ?: emptySet()
                val selectedTags =
                    if (current.contains(intent.tag))
                        current - intent.tag
                    else
                        current + intent.tag

                _state.update {
                    it.copy(selectedTags = selectedTags, showAddTag = false)
                }
            }

            is TagIntent.SetAllSelectedTags -> _state.update {
                it.copy(selectedTags = intent.tags)
            }

            is TagIntent.ShowAddTag -> _state.update {
                it.copy(showAddTag = intent.showAddTag)
            }

        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllTag().collect { tags ->
//                val tagSet = tags.map { tag -> (tag.id?.toInt() ?: -1) to tag.name }.toSet()
                _state.update { it.copy(tags = tags, isLoading = false) }
            }
        }
    }


}

data class TagState(
    val tags: List<Tag> = emptyList(),
    val selectedTags: Set<Pair<Int, String>>? = null,
    val showAddTag: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface TagIntent {
    data class SetSelectedTag(val tag: Pair<Int, String>) : TagIntent
    data class SetAllSelectedTags(val tags: Set<Pair<Int, String>>? = null) : TagIntent
    data object GetAllTag : TagIntent
    data class ShowAddTag(val showAddTag: Boolean = false) : TagIntent
}