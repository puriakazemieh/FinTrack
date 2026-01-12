package com.kazemieh.category.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddCategory
import com.kazemieh.domain.usecase.DeleteCategory
import com.kazemieh.domain.usecase.EditCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategory,
    private val editCategoryUseCase: EditCategory
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
            is AddCategoryIntent.ShowEditData -> _state.update {
                it.copy(
                    selectedCategory = intent.selectedCategory,
                    categoryName = intent.selectedCategory.name,
                    description = intent.selectedCategory.description,
                    categoryType = intent.selectedCategory.type
                )
            }
        }
    }

    private fun addCategory() = with(_state.value) {
        viewModelScope.launch {
            if (categoryName?.isNotBlank() == true) {
                val category = Category(
                    id = selectedCategory?.id,
                    name = categoryName,
                    description = description,
                    type = categoryType
                )
                val categoryId = if (selectedCategory != null) {
                    editCategoryUseCase(category).toLong()
                } else {
                    addCategoryUseCase(category)
                }
                if (categoryId >= 0) {
                    _effect.send(AddCategoryEffect.AddedCategory(category.copy(id = categoryId)))
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
    val categoryType: TransactionType = TransactionType.INCOME,
    val selectedCategory: Category? = null
)

sealed interface AddCategoryIntent {
    data object AddCategory : AddCategoryIntent
    data class SetCategoryName(val categoryName: String? = null) : AddCategoryIntent
    data class SetCategoryType(val categoryType: TransactionType) : AddCategoryIntent
    data class ShowEditData(val selectedCategory: Category) : AddCategoryIntent
    data class SetDescription(val description: String? = null) : AddCategoryIntent
    data object OnDismiss : AddCategoryIntent
}

sealed interface AddCategoryEffect {
    data class ShowMessage(val message: Int) : AddCategoryEffect
    data class AddedCategory(val category: Category) : AddCategoryEffect
    data object OnDismiss : AddCategoryEffect
}