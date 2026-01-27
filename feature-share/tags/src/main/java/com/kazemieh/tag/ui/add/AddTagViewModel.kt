package com.kazemieh.tag.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddTag
import com.kazemieh.domain.usecase.EditTag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTagViewModel(
    private val addTagUseCase: AddTag,
    private val editTag: EditTag
) : ViewModel() {

    private val _state = MutableStateFlow(AddTagState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddTagEffect>()
    val effect = _effect.receiveAsFlow()


    fun onIntent(intent: AddTagIntent) {
        when (intent) {
            AddTagIntent.AddTag -> addTag()
            AddTagIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddTagState() }
                    _effect.send(AddTagEffect.OnDismiss)
                }
            }

            is AddTagIntent.SetTagName -> _state.update { it.copy(tagName = intent.tagName) }
            is AddTagIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddTagIntent.ShowEditData -> _state.update {
                it.copy(
                    description = intent.tag.description,
                    tagName = intent.tag.name,
                    tag = intent.tag
                )
            }
        }
    }

    private fun addTag() = with(_state.value) {
        viewModelScope.launch {
            if (tagName?.isNotBlank() == true) {
                val tag = Tag(
                    id = tag?.id,
                    name = tagName,
                    description = description
                )
                val tagId = if (_state.value.tag != null) {
                    editTag(tag).toLong()
                } else {
                    addTagUseCase(tag)
                }
                if (tagId >= 0) {
                    _effect.send(AddTagEffect.AddedTag(tag.copy(id = tagId)))
                    _state.update { AddTagState() }
                }

            } else {
                _effect.send(AddTagEffect.ShowMessage(R.string.check_name_tag_source))
            }
        }
    }
}


data class AddTagState(
    val tagName: String? = null,
    val description: String? = null,
    val tag: Tag? = null,
)

sealed interface AddTagIntent {
    data object AddTag : AddTagIntent
    data class SetTagName(val tagName: String? = null) : AddTagIntent
    data class SetDescription(val description: String? = null) : AddTagIntent
    data object OnDismiss : AddTagIntent
    data class ShowEditData(val tag: Tag) : AddTagIntent
}

sealed interface AddTagEffect {
    data class ShowMessage(val message: Int) : AddTagEffect
    data class AddedTag(val tag: Tag) : AddTagEffect
    data object OnDismiss : AddTagEffect
}