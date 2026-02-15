package com.kazemieh.category.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddCategoryUseCase
import com.kazemieh.domain.usecase.UpdateCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategory
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddCategoryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddCategoryIntent) {
        when (intent) {
            AddCategoryIntent.Save -> save()
            AddCategoryIntent.OnDismiss -> dismiss()

            is AddCategoryIntent.UpdateName -> updateDraft { it.copy(name = intent.value) }
            is AddCategoryIntent.UpdateDescription -> updateDraft { it.copy(description = intent.value) }
            is AddCategoryIntent.UpdateType -> updateDraft { it.copy(type = intent.value) }

            is AddCategoryIntent.SetColorIcon -> _state.update {
                it.copy(
                    draft = it.draft.copy(
                        colorId = intent.colorId,
                        iconId = intent.iconId
                    ),
                    isPickerOpen = false
                )
            }

            is AddCategoryIntent.StartEdit -> startEdit(intent.category)
            is AddCategoryIntent.StartAdd -> startAdd(intent.type)

            AddCategoryIntent.OpenPicker -> _state.update { it.copy(isPickerOpen = true) }
            AddCategoryIntent.ClosePicker -> _state.update { it.copy(isPickerOpen = false) }
        }
    }

    private fun updateDraft(transform: (CategoryDraft) -> CategoryDraft) {
        _state.update { it.copy(draft = transform(it.draft)) }
    }

    private fun startAdd(type: TransactionType) {
        _state.update {
            AddCategoryState(
                mode = AddCategoryMode.Add,
                draft = CategoryDraft(type = type),
                isPickerOpen = false
            )
        }
    }

    private fun startEdit(category: Category) {
        _state.update {
            AddCategoryState(
                mode = AddCategoryMode.Edit(category.id ?: 0),
                draft = category.toDraft(),
                isPickerOpen = false
            )
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _state.update { AddCategoryState() }
            _effect.send(AddCategoryEffect.OnDismiss)
        }
    }

    private fun save() = with(_state.value) {
        viewModelScope.launch {
            val name = draft.name.trim()
            if (name.isBlank()) {
                _effect.send(AddCategoryEffect.ShowMessage(R.string.check_name_category_source))
                return@launch
            }

            val category = draft.toCategory(id = mode.categoryIdOrNull)

            val categoryId = when (mode) {
                is AddCategoryMode.Add -> addCategoryUseCase(category)
                is AddCategoryMode.Edit -> updateCategoryUseCase(category).toLong()
            }

            if (categoryId >= 0) {
                val saved = category.copy(id = categoryId)
                _effect.send(AddCategoryEffect.SavedCategory(saved))
                _state.update { AddCategoryState() }
            }
        }
    }
}

/** --- State / Mode / Draft --- **/

data class AddCategoryState(
    val mode: AddCategoryMode = AddCategoryMode.Add,
    val draft: CategoryDraft = CategoryDraft(),
    val isPickerOpen: Boolean = false
)

sealed interface AddCategoryMode {
    data object Add : AddCategoryMode
    data class Edit(val categoryId: Long) : AddCategoryMode
}

private val AddCategoryMode.categoryIdOrNull: Long?
    get() = (this as? AddCategoryMode.Edit)?.categoryId

data class CategoryDraft(
    val name: String = "",
    val description: String? = null,
    val colorId: Int? = null,
    val iconId: Int? = null,
    val type: TransactionType = TransactionType.INCOME
)

private fun Category.toDraft(): CategoryDraft = CategoryDraft(
    name = this.name.orEmpty(),
    description = this.description,
    colorId = this.colorId,
    iconId = this.iconId,
    type = this.type
)

private fun CategoryDraft.toCategory(id: Long?): Category = Category(
    id = id,
    name = name,
    description = description,
    type = type,
    colorId = colorId ?: 1,
    iconId = iconId ?: 1
)

/** --- Intent / Effect --- **/

sealed interface AddCategoryIntent {
    data object Save : AddCategoryIntent
    data object OnDismiss : AddCategoryIntent

    data class StartAdd(val type: TransactionType) : AddCategoryIntent
    data class StartEdit(val category: Category) : AddCategoryIntent

    data class UpdateName(val value: String) : AddCategoryIntent
    data class UpdateDescription(val value: String?) : AddCategoryIntent
    data class UpdateType(val value: TransactionType) : AddCategoryIntent

    data class SetColorIcon(val colorId: Int?, val iconId: Int?) : AddCategoryIntent

    data object OpenPicker : AddCategoryIntent
    data object ClosePicker : AddCategoryIntent
}

sealed interface AddCategoryEffect {
    data class ShowMessage(val message: Int) : AddCategoryEffect
    data class SavedCategory(val category: Category) : AddCategoryEffect
    data object OnDismiss : AddCategoryEffect
}
