package com.kazemieh.category.ui.delete


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.domain.usecase.TransactionUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeleteCategoryViewModel(
    private val deleteCategoryUseCase: com.kazemieh.domain.usecase.DeleteCategory
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteCategoryState())
    val state: StateFlow<DeleteCategoryState> = _state.asStateFlow()

    private val _effect = Channel<DeleteTransactionEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeleteCategory) {
        when (intent) {

            is DeleteCategory.SetData -> _state.update { it.copy(category = intent.category) }

            DeleteCategory.Submit -> deleteCategory()

            DeleteCategory.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { DeleteCategoryState() }
                    _effect.send(DeleteTransactionEffect.OnDismiss)
                }
            }

        }
    }

    private fun deleteCategory() {
        viewModelScope.launch {
            _state.value.category?.let {
                deleteCategoryUseCase(it)
                _effect.send(DeleteTransactionEffect.DeletedTransaction)
            }
        }
    }

}

sealed interface DeleteCategory {
    data object Submit : DeleteCategory
    data object OnDismiss : DeleteCategory
    data class SetData(val category: Category? = null) : DeleteCategory
}


data class DeleteCategoryState(
    val category: Category? = null
)

sealed interface DeleteTransactionEffect {
    object DeletedTransaction : DeleteTransactionEffect
    data class ShowMessage(val message: Int) : DeleteTransactionEffect
    data object OnDismiss : DeleteTransactionEffect
}
