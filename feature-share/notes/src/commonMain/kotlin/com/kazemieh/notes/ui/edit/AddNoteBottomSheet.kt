package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.Note
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteBottomSheet(
    note: Note? = null,
    onDismiss: () -> Unit,
    viewModel: NoteEditViewModel = koinViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(note) {
        viewModel.onIntent(NoteEditIntent.LoadNote(note?.id ?: 0L))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is NoteEditEffect.SaveSuccess) {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            NoteEditContent(
                onBack = onDismiss,
                viewModel = viewModel,
                noteId = note?.id ?: 0L
            )
        }
    }
}
