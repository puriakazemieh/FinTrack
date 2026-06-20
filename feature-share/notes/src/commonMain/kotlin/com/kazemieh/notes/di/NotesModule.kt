package com.kazemieh.notes.di

import com.kazemieh.notes.ui.edit.NoteEditViewModel
import com.kazemieh.notes.ui.list.NotesViewModel
import com.kazemieh.domain.usecase.AddNoteUseCase
import com.kazemieh.domain.usecase.DeleteNoteUseCase
import com.kazemieh.domain.usecase.GetNoteByIdUseCase
import com.kazemieh.domain.usecase.ObserveNotesUseCase
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.ToggleNoteLockUseCase
import com.kazemieh.domain.usecase.ToggleNotePinUseCase
import com.kazemieh.domain.usecase.UpdateNoteUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notesModule = module {
    factory { ObserveNotesUseCase(get()) }
    factory { GetNoteByIdUseCase(get()) }
    factory { AddNoteUseCase(get()) }
    factory { UpdateNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { ToggleNotePinUseCase(get()) }
    factory { ToggleNoteLockUseCase(get()) }
    factory { ObserveTagsUseCase(get()) }

    viewModelOf(::NotesViewModel)
    viewModelOf(::NoteEditViewModel)
}
