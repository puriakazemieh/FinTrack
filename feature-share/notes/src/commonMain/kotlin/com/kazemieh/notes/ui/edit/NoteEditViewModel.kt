package com.kazemieh.notes.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Note
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.usecase.AddNoteUseCase
import com.kazemieh.domain.usecase.GetNoteByIdUseCase
import com.kazemieh.domain.usecase.UpdateNoteUseCase
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.ToggleNotePinUseCase
import com.kazemieh.domain.usecase.ToggleNoteLockUseCase
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.NotificationManager
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.note_not_found
import fintrack.core.designsystem.generated.resources.notes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

data class NoteEditState(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: Long = 0L, // 0 means default theme color
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val reminderTime: Long? = null,
    val selectedTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface NoteEditIntent {
    data class LoadNote(val id: Long) : NoteEditIntent
    data class OnTitleChanged(val title: String) : NoteEditIntent
    data class OnContentChanged(val content: String) : NoteEditIntent
    data class OnColorChanged(val color: Long) : NoteEditIntent
    data class OnTagsChanged(val tags: List<Tag>) : NoteEditIntent
    data class OnReminderChanged(val time: Long?) : NoteEditIntent
    data object OnTogglePin : NoteEditIntent
    data object OnToggleLock : NoteEditIntent
    data object OnSave : NoteEditIntent
}

sealed interface NoteEditEffect {
    data object SaveSuccess : NoteEditEffect
    data class ShowError(val message: String) : NoteEditEffect
}

class NoteEditViewModel(
    private val getNoteById: GetNoteByIdUseCase,
    private val addNote: AddNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val observeTags: ObserveTagsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val togglePinUseCase: ToggleNotePinUseCase,
    private val toggleLockUseCase: ToggleNoteLockUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditState())
    val state = _state.asStateFlow()

    private val _effect = Channel<NoteEditEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeTags().onEach { tags ->
                _state.update { it.copy(allTags = tags) }
            }.launchIn(this)
        }
    }

    fun onIntent(intent: NoteEditIntent) {
        when (intent) {
            is NoteEditIntent.LoadNote -> loadNote(intent.id)
            is NoteEditIntent.OnTitleChanged -> _state.update { it.copy(title = intent.title) }
            is NoteEditIntent.OnContentChanged -> _state.update { it.copy(content = intent.content) }
            is NoteEditIntent.OnColorChanged -> _state.update { it.copy(color = intent.color) }
            is NoteEditIntent.OnTagsChanged -> _state.update { it.copy(selectedTags = intent.tags) }
            is NoteEditIntent.OnReminderChanged -> _state.update { it.copy(reminderTime = intent.time) }
            NoteEditIntent.OnTogglePin -> togglePin()
            NoteEditIntent.OnToggleLock -> toggleLock()
            NoteEditIntent.OnSave -> saveNote()
        }
    }

    private fun loadNote(id: Long) {
        if (id == 0L) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val note = getNoteById(id)
            if (note != null) {
                _state.update {
                    it.copy(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        color = note.color,
                        isPinned = note.isPinned,
                        isLocked = note.isLocked,
                        reminderTime = note.reminderTime,
                        selectedTags = note.tags,
                        isLoading = false
                    )
                }
            } else {
                _effect.send(NoteEditEffect.ShowError(getString(Res.string.note_not_found)))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun togglePin() {
        val current = _state.value.isPinned
        _state.update { it.copy(isPinned = !current) }
        if (_state.value.id != 0L) {
            viewModelScope.launch {
                togglePinUseCase(_state.value.id, !current)
            }
        }
    }

    private fun toggleLock() {
        val current = _state.value.isLocked
        _state.update { it.copy(isLocked = !current) }
        if (_state.value.id != 0L) {
            viewModelScope.launch {
                toggleLockUseCase(_state.value.id, !current)
            }
        }
    }

    private fun saveNote() {
        viewModelScope.launch {
            val current = _state.value
            val note = Note(
                id = current.id,
                title = current.title,
                content = current.content,
                color = current.color,
                isPinned = current.isPinned,
                isLocked = current.isLocked,
                reminderTime = current.reminderTime,
                tags = current.selectedTags
            )
            val noteId = if (note.id == 0L) addNote(note) else { updateNote(note); note.id }
            syncReminder(noteId, current.reminderTime, current.title.ifBlank { current.content })
            _effect.send(NoteEditEffect.SaveSuccess)
        }
    }

    private suspend fun syncReminder(noteId: Long, reminderTime: Long?, message: String) {
        val reminderId = "note_$noteId"
        if (reminderTime == null) {
            notificationScheduler.cancelReminder(reminderId)
            return
        }
        notificationScheduler.scheduleReminder(
            id = reminderId,
            title = getString(Res.string.notes),
            message = message,
            scheduledTime = Instant.fromEpochMilliseconds(reminderTime)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            channelId = NotificationManager.CHANNEL_NOTE
        )
    }
}
