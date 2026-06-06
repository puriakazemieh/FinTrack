package com.kazemieh.designsystem.component

import com.kazemieh.designsystem.component.model.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global singleton for managing snackbar notifications across modules
 */
object SnackbarController {
    private val _events = MutableSharedFlow<SnackbarEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    suspend fun showMessage(message: UiText) {
        _events.emit(SnackbarEvent(message))
    }
}

data class SnackbarEvent(
    val message: UiText
)
