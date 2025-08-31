package com.kazemieh.category.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.AddCategory
import kotlinx.coroutines.launch

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategory
) : ViewModel() {

    fun addCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                addCategoryUseCase(name)
            }
        }
    }
}