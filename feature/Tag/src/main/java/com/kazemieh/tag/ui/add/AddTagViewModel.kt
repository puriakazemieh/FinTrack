package com.kazemieh.tag.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.AddTag
import kotlinx.coroutines.launch

class AddTagViewModel(
    private val addTagUseCase: AddTag
) : ViewModel() {

    fun addTag(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                addTagUseCase(name)
            }
        }
    }
}