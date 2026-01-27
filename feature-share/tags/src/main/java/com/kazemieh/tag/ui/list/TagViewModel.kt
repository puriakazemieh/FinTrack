package com.kazemieh.tag.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.ItemUi
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.toItemUi
import com.kazemieh.domain.usecase.GetAllTag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagViewModel(
    private val getAllTag: GetAllTag
) : ViewModel() {

    private val _state = MutableStateFlow(TagState())
    val state = _state.asStateFlow()

    private val _effect = Channel<TagEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: TagIntent) {
        when (intent) {
            TagIntent.GetAllTag -> loadAllTags()

            is TagIntent.SetSelectedTag -> {
                val current = _state.value.initialSelectionItem
                val person = intent.tag
                val selectedPersons =
                    if (current.contains(person))
                        current - person
                    else
                        current + person

                _state.update {
                    it.copy(initialSelectionItem = selectedPersons, showAddTag = false)
                }
            }

            is TagIntent.SetAllSelectedTags -> _state.update {
                it.copy(initialSelectionItem = intent.tags ?: emptySet())
            }

            is TagIntent.ShowAddTag -> _state.update {
                it.copy(showAddTag = !state.value.showAddTag, selectedTag = null)
            }

            is TagIntent.OnDeleteClick -> _state.update {
                it.copy(
                    isDeleteShow = !_state.value.isDeleteShow,
                    selectedTag = intent.tag
                )
            }

            is TagIntent.OnEditClick -> _state.update {
                it.copy(
                    showAddTag = true,
                    selectedTag = intent.tag
                )
            }

            TagIntent.OnDismiss -> {
                viewModelScope.launch {
                    _effect.send(TagEffect.OnDismiss)
                    _state.update { TagState() }
                }
            }

            is TagIntent.SelectedTag -> {
                viewModelScope.launch {
                    if (intent.tag != null) {
                        _effect.send(TagEffect.OnTagSelected(intent.tag))
                        _state.update { TagState() }
                    }
                }
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
                        items = tags.map { it.toItemUi() }.toSet(),
                        isLoading = false
                    )
                }
            }
        }
    }


}

data class TagState(
    val tags: List<Tag> = emptyList(),
    val selectedTag: Tag? = null,
    val showAddTag: Boolean = false,
    val isLoading: Boolean = false,
    val initialSelectionItem: Set<Tag> = emptySet(),
    val items: Set<ItemUi> = emptySet(),
    val isDeleteShow: Boolean = false,
)

sealed interface TagIntent {
    data class SetSelectedTag(val tag: Tag) : TagIntent
    data class SetAllSelectedTags(val tags: Set<Tag>? = null) : TagIntent
    data object GetAllTag : TagIntent
    data object ShowAddTag : TagIntent
    data object OnDismiss : TagIntent
    data class SelectedTag(val tag: Tag? = null) : TagIntent
    data class OnEditClick(val tag: Tag? = null) : TagIntent
    data class OnDeleteClick(val tag: Tag? = null) : TagIntent
}


sealed interface TagEffect {
    data object OnDismiss : TagEffect
    data class OnTagSelected(val tag: Tag) : TagEffect
}