package com.kazemieh.category.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddCategory
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategory
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddCategoryEffect>()
    val effect = _effect.receiveAsFlow()


    fun onIntent(intent: AddCategoryIntent) {
        when (intent) {
            AddCategoryIntent.AddCategory -> addCategory()
            AddCategoryIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddCategoryState() }
                    _effect.send(AddCategoryEffect.OnDismiss)
                }
            }

            is AddCategoryIntent.SetCategoryName -> _state.update { it.copy(categoryName = intent.categoryName) }
            is AddCategoryIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddCategoryIntent.SetCategoryType -> _state.update { it.copy(categoryType = intent.categoryType) }
        }
    }

    private fun addCategory() = with(_state.value) {
        viewModelScope.launch {
            if (categoryName?.isNotBlank() == true) {
                val category = Category(
                    name = categoryName,
                    description = description,
                    type = TransactionType.fromInt(categoryType)
                )
                val categoryId = addCategoryUseCase(category)
                if (categoryId >= 0) {
                    _effect.send(
                        AddCategoryEffect.AddedCategory(
                            categoryId.toInt(),
                            _state.value.categoryName ?: ""
                        )
                    )
                    _state.update { AddCategoryState() }
                }

            } else {
                _effect.send(AddCategoryEffect.ShowMessage(R.string.check_name_category_source))
            }
        }
    }
}


data class AddCategoryState(
    val categoryName: String? = null,
    val description: String? = null,
    val categoryType: Int = 1,
)

sealed interface AddCategoryIntent {
    data object AddCategory : AddCategoryIntent
    data class SetCategoryName(val categoryName: String? = null) : AddCategoryIntent
    data class SetCategoryType(val categoryType: Int) : AddCategoryIntent
    data class SetDescription(val description: String? = null) : AddCategoryIntent
    data object OnDismiss : AddCategoryIntent
}

sealed interface AddCategoryEffect {
    data class ShowMessage(val message: Int) : AddCategoryEffect
    data class AddedCategory(val id: Int, val name: String) : AddCategoryEffect
    data object OnDismiss : AddCategoryEffect
}