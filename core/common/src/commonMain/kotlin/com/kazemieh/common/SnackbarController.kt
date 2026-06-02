package com.kazemieh.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Global singleton for managing snackbar notifications across modules
 */
object SnackbarController {
    private val _events = Channel<SnackbarEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun showMessage(message: String) {
        _events.send(SnackbarEvent(message))
    }
}

data class SnackbarEvent(
    val message: String
)
