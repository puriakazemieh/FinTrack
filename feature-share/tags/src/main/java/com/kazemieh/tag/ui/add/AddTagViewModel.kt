package com.kazemieh.tag.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.AddTag
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTagViewModel(
    private val addTagUseCase: AddTag
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
        }
    }

    private fun addTag() = with(_state.value) {
        viewModelScope.launch {
            if (tagName?.isNotBlank() == true) {
                val tag = Tag(
                    name = tagName,
                    description = description
                )
                val tagId = addTagUseCase(tag)
                if (tagId >= 0) {
                    _effect.send(AddTagEffect.AddedTag(tag))
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
)

sealed interface AddTagIntent {
    data object AddTag : AddTagIntent
    data class SetTagName(val tagName: String? = null) : AddTagIntent
    data class SetDescription(val description: String? = null) : AddTagIntent
    data object OnDismiss : AddTagIntent
}

sealed interface AddTagEffect {
    data class ShowMessage(val message: Int) : AddTagEffect
    data class AddedTag(val tag: Tag) : AddTagEffect
    data object OnDismiss : AddTagEffect
}