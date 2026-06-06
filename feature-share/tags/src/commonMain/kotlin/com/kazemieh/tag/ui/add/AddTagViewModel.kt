package com.kazemieh.tag.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.AddTagUseCase
import com.kazemieh.domain.usecase.UpdateTagUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.check_name_tag_source
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTagViewModel(
    private val addTagUseCase: AddTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddTagState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddTagEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddTagIntent) {
        when (intent) {
            is AddTagIntent.StartAdd -> startAdd()
            is AddTagIntent.StartEdit -> startEdit(intent.tag)

            is AddTagIntent.UpdateName -> updateDraft { it.copy(name = intent.value) }
            is AddTagIntent.UpdateDescription -> updateDraft { it.copy(description = intent.value) }

            AddTagIntent.OpenPicker -> _state.update { it.copy(isPickerOpen = true) }
            AddTagIntent.ClosePicker -> _state.update { it.copy(isPickerOpen = false) }
            is AddTagIntent.SetColorIcon -> _state.update {
                it.copy(
                    draft = it.draft.copy(colorId = intent.colorId, iconId = intent.iconId),
                    isPickerOpen = false
                )
            }

            AddTagIntent.Save -> save()
            AddTagIntent.OnDismiss -> dismiss()
        }
    }

    private fun updateDraft(transform: (TagDraft) -> TagDraft) {
        _state.update { it.copy(draft = transform(it.draft)) }
    }

    private fun startAdd() {
        _state.update {
            AddTagState(
                mode = AddTagMode.Add,
                draft = TagDraft(),
                isPickerOpen = false
            )
        }
    }

    private fun startEdit(tag: Tag) {
        _state.update {
            AddTagState(
                mode = AddTagMode.Edit(tag.id ?: 0L),
                draft = tag.toDraft(),
                isPickerOpen = false
            )
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _state.update { AddTagState() }
            _effect.send(AddTagEffect.OnDismiss)
        }
    }

    private fun save() = with(_state.value) {
        viewModelScope.launch {
            val name = draft.name.trim()
            if (name.isBlank()) {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.check_name_tag_source))
                return@launch
            }

            val tag = draft.toTag(id = mode.tagIdOrNull)

            val tagId = when (mode) {
                is AddTagMode.Add -> addTagUseCase(tag)
                is AddTagMode.Edit -> updateTagUseCase(tag).toLong()
            }

            if (tagId >= 0) {
                val saved = tag.copy(id = tagId)
                _effect.send(AddTagEffect.SavedTag(saved))
                _state.update { AddTagState() }
            }
        }
    }
}

/** --- State / Mode / Draft --- **/

data class AddTagState(
    val mode: AddTagMode = AddTagMode.Add,
    val draft: TagDraft = TagDraft(),
    val isPickerOpen: Boolean = false
)

sealed interface AddTagMode {
    data object Add : AddTagMode
    data class Edit(val tagId: Long) : AddTagMode
}

private val AddTagMode.tagIdOrNull: Long?
    get() = (this as? AddTagMode.Edit)?.tagId

data class TagDraft(
    val name: String = "",
    val description: String? = null,
    val colorId: Int? = null,
    val iconId: Int? = null
)

private fun Tag.toDraft(): TagDraft = TagDraft(
    name = this.name.orEmpty(),
    description = this.description,
    colorId = this.colorId,
    iconId = this.iconId
)

private fun TagDraft.toTag(id: Long?): Tag = Tag(
    id = id,
    name = name,
    description = description,
    colorId = colorId ?: 1,
    iconId = iconId ?: 1
)

/** --- Intent / Effect --- **/

sealed interface AddTagIntent {
    data object StartAdd : AddTagIntent
    data class StartEdit(val tag: Tag) : AddTagIntent

    data class UpdateName(val value: String) : AddTagIntent
    data class UpdateDescription(val value: String?) : AddTagIntent

    data object OpenPicker : AddTagIntent
    data object ClosePicker : AddTagIntent
    data class SetColorIcon(val colorId: Int?, val iconId: Int?) : AddTagIntent

    data object Save : AddTagIntent
    data object OnDismiss : AddTagIntent
}

sealed interface AddTagEffect {
    data class SavedTag(val tag: Tag) : AddTagEffect
    data object OnDismiss : AddTagEffect
}
