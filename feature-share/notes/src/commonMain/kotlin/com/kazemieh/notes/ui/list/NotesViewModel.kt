package com.kazemieh.notes.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Note
import com.kazemieh.domain.usecase.DeleteNoteUseCase
import com.kazemieh.domain.usecase.ObserveNotesUseCase
import com.kazemieh.domain.usecase.ToggleNoteLockUseCase
import com.kazemieh.domain.usecase.ToggleNotePinUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface NotesIntent {
    data class OnSearchQueryChanged(val query: String) : NotesIntent
    data class OnTogglePin(val note: Note) : NotesIntent
    data class OnToggleLock(val note: Note) : NotesIntent
    data class OnDeleteNote(val id: Long) : NotesIntent
}

sealed interface NotesEffect {
    data class ShowMessage(val message: String) : NotesEffect
}

class NotesViewModel(
    private val observeNotes: ObserveNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val togglePin: ToggleNotePinUseCase,
    private val toggleLock: ToggleNoteLockUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state = _state.asStateFlow()

    private val _effect = Channel<NotesEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        _state.update { it.copy(isLoading = true) }
        observeNotes()
            .onEach { notes ->
                _state.update { it.copy(notes = notes, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is NotesIntent.OnTogglePin -> {
                viewModelScope.launch {
                    togglePin(intent.note.id, !intent.note.isPinned)
                }
            }
            is NotesIntent.OnToggleLock -> {
                viewModelScope.launch {
                    toggleLock(intent.note.id, !intent.note.isLocked)
                }
            }
            is NotesIntent.OnDeleteNote -> {
                viewModelScope.launch {
                    deleteNote(intent.id)
                }
            }
        }
    }
}
