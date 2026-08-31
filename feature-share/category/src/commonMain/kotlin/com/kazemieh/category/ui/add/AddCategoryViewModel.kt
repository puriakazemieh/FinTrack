package com.kazemieh.category.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.AddCategoryUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.UpdateCategoryUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.check_name_category_source
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AddCategoryViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddCategoryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        _state.map { it.draft.type }
            .distinctUntilChanged()
            .flatMapLatest { type ->
                observeCategoriesUseCase(type, parentId = null)
            }
            .onEach { categories ->
                _state.update { it.copy(parentCategories = categories) }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: AddCategoryIntent) {
        when (intent) {
            AddCategoryIntent.Save -> save()
            AddCategoryIntent.OnDismiss -> dismiss()

            is AddCategoryIntent.UpdateName -> updateDraft { it.copy(name = intent.value) }
            is AddCategoryIntent.UpdateDescription -> updateDraft { it.copy(description = intent.value) }
            is AddCategoryIntent.UpdateType -> updateDraft {
                it.copy(
                    type = intent.value,
                    parentId = null
                )
            }

            is AddCategoryIntent.UpdateParentId -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.CategoryParentChanged)
                updateDraft { it.copy(parentId = intent.parentId) }
            }

            is AddCategoryIntent.SetColorIcon -> {
                if (intent.iconId != _state.value.draft.iconId || intent.colorId != _state.value.draft.colorId) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.CategoryIconChanged)
                }
                _state.update {
                    it.copy(
                        draft = it.draft.copy(
                            colorId = intent.colorId,
                            iconId = intent.iconId
                        ),
                        isPickerOpen = false
                    )
                }
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
            it.copy(
                mode = AddCategoryMode.Add,
                draft = CategoryDraft(type = type),
                isPickerOpen = false
            )
        }
    }

    private fun startEdit(category: Category) {
        _state.update {
            it.copy(
                mode = AddCategoryMode.Edit(category.id ?: 0),
                draft = category.toDraft(),
                isPickerOpen = false
            )
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _state.update { it.copy(mode = AddCategoryMode.Add, draft = CategoryDraft()) }
            _effect.send(AddCategoryEffect.OnDismiss)
        }
    }

    private fun save() = with(_state.value) {
        viewModelScope.launch {
            val name = draft.name.trim()
            if (name.isBlank()) {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.check_name_category_source))
                return@launch
            }

            val category = draft.toCategory(id = mode.categoryIdOrNull)

            val categoryId = when (mode) {
                is AddCategoryMode.Add -> {
                    val id = addCategoryUseCase(category)
                    if (id > 0) analytics.track(com.kazemieh.common.analytics.ProductEvent.CategoryCreated(if(category.parentId == null) "parent" else "sub"))
                    id
                }
                is AddCategoryMode.Edit -> {
                    val id = updateCategoryUseCase(category).toLong()
                    if (id > 0) analytics.track(com.kazemieh.common.analytics.ProductEvent.CategoryUpdated)
                    id
                }
            }

            if (categoryId >= 0) {
                val saved = category.copy(id = categoryId)
                _effect.send(AddCategoryEffect.SavedCategory(saved))
                _state.update { it.copy(mode = AddCategoryMode.Add, draft = CategoryDraft()) }
            }
        }
    }
}

/** --- State / Mode / Draft --- **/

data class AddCategoryState(
    val mode: AddCategoryMode = AddCategoryMode.Add,
    val draft: CategoryDraft = CategoryDraft(),
    val isPickerOpen: Boolean = false,
    val parentCategories: List<Category> = emptyList()
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
    val type: TransactionType = TransactionType.INCOME,
    val parentId: Long? = null
)

private fun Category.toDraft(): CategoryDraft = CategoryDraft(
    name = this.name.orEmpty(),
    description = this.description,
    colorId = this.colorId,
    iconId = this.iconId,
    type = this.type,
    parentId = this.parentId
)

private fun CategoryDraft.toCategory(id: Long?): Category = Category(
    id = id,
    name = name,
    description = description,
    type = type,
    colorId = colorId ?: 1,
    iconId = iconId ?: 1,
    parentId = parentId
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
    data class UpdateParentId(val parentId: Long?) : AddCategoryIntent

    data class SetColorIcon(val colorId: Int?, val iconId: Int?) : AddCategoryIntent

    data object OpenPicker : AddCategoryIntent
    data object ClosePicker : AddCategoryIntent
}

sealed interface AddCategoryEffect {
    data class SavedCategory(val category: Category) : AddCategoryEffect
    data object OnDismiss : AddCategoryEffect
}
