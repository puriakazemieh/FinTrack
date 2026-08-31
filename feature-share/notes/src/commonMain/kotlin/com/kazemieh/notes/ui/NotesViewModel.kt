package com.kazemieh.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Note
import com.kazemieh.domain.usecase.DeleteNoteUseCase
import com.kazemieh.domain.usecase.ObserveNotesUseCase
import com.kazemieh.domain.usecase.ToggleNoteLockUseCase
import com.kazemieh.domain.usecase.ToggleNotePinUseCase
import com.kazemieh.domain.usecase.UpdateNoteUseCase
import com.kazemieh.notes.ui.edit.toggleCheckboxLine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class NotesState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface NotesIntent {
    data class OnSearchQueryChanged(val query: String) : NotesIntent
    data class OnTogglePin(val note: Note) : NotesIntent
    data class OnToggleLock(val note: Note) : NotesIntent
    data class OnDeleteNote(val id: Long) : NotesIntent
    data class OnToggleCheckbox(val note: Note, val lineIndex: Int) : NotesIntent
}

sealed interface NotesEffect {
    data class ShowMessage(val message: String) : NotesEffect
}

class NotesViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val observeNotes: ObserveNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val togglePin: ToggleNotePinUseCase,
    private val toggleLock: ToggleNoteLockUseCase,
    private val updateNote: UpdateNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state = _state.asStateFlow()

    private val _effect = Channel<NotesEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("note_list"))
        _state.update { it.copy(isLoading = true) }
        observeNotes()
            .combine(_searchQuery) { notes, query ->
                val filtered = notes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
                notes to filtered
            }
            .onEach { (all, filtered) ->
                _state.update {
                    it.copy(
                        notes = all,
                        filteredNotes = filtered,
                        isLoading = false,
                        searchQuery = _searchQuery.value
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.OnSearchQueryChanged -> {
                _searchQuery.value = intent.query
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
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.NoteDeleted)
                }
            }
            is NotesIntent.OnToggleCheckbox -> {
                viewModelScope.launch {
                    val newContent = toggleCheckboxLine(intent.note.content, intent.lineIndex)
                    updateNote(intent.note.copy(content = newContent))
                }
            }
        }
    }
}
