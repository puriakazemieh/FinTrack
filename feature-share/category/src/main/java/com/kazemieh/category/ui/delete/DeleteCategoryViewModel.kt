package com.kazemieh.category.ui.delete


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.DeleteCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeleteCategoryViewModel(
    private val deleteCategoryUseCase: DeleteCategory
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteCategoryState())
    val state: StateFlow<DeleteCategoryState> = _state.asStateFlow()

    private val _effect = Channel<DeleteCategoryEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeleteCategoryIntent) {
        when (intent) {
            is DeleteCategoryIntent.SetData -> _state.update { it.copy(category = intent.category) }

            DeleteCategoryIntent.Submit -> deleteCategory()
            DeleteCategoryIntent.DeleteAllTransaction -> _state.update {
                it.copy(
                    isDeleteAllData = true,
                    moveCategory = null,
                    isCategoryError = false
                )
            }

            DeleteCategoryIntent.MoveAllTransaction -> _state.update { it.copy(isDeleteAllData = false) }
            DeleteCategoryIntent.Dismiss -> viewModelScope.launch {
                _state.update { DeleteCategoryState() }
                _effect.send(DeleteCategoryEffect.OnDismiss)
            }

            DeleteCategoryIntent.ShowAllCategoryList -> _state.update {
                it.copy(
                    isCategoryListShow = !_state.value.isCategoryListShow,
                    moveCategory = if (!_state.value.isCategoryListShow) _state.value.moveCategory else null
                )
            }

            is DeleteCategoryIntent.SetMoveCategory -> _state.update {
                it.copy(
                    moveCategory = intent.category,
                    isCategoryListShow = false
                )
            }
        }
    }

    private fun deleteCategory() {
        viewModelScope.launch {
            val isError = !_state.value.isDeleteAllData && _state.value.moveCategory == null
            if (isError) {
                _state.update { it.copy(isCategoryError = true) }
                _effect.send(DeleteCategoryEffect.ShowMessage(R.string.category_choose))
                return@launch
            }
            _state.value.category?.let { deleteCategory ->
                deleteCategoryUseCase(deleteCategory, _state.value.moveCategory)
                _effect.send(DeleteCategoryEffect.DeletedTransaction)
            }
        }
    }

}

sealed interface DeleteCategoryIntent {
    data object Submit : DeleteCategoryIntent
    data object DeleteAllTransaction : DeleteCategoryIntent
    data object ShowAllCategoryList : DeleteCategoryIntent
    data object Dismiss : DeleteCategoryIntent
    data object MoveAllTransaction : DeleteCategoryIntent
    data class SetData(val category: Category? = null) : DeleteCategoryIntent
    data class SetMoveCategory(val category: Category? = null) : DeleteCategoryIntent
}

data class DeleteCategoryState(
    val category: Category? = null,
    val moveCategory: Category? = null,
    val isCategoryError: Boolean = false,
    val isCategoryListShow: Boolean = false,
    val isDeleteAllData: Boolean = true
)

sealed interface DeleteCategoryEffect {
    data class ShowMessage(val message: Int) : DeleteCategoryEffect
    object DeletedTransaction : DeleteCategoryEffect
    data object OnDismiss : DeleteCategoryEffect
}
