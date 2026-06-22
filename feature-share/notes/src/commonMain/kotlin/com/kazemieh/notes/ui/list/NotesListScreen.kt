package com.kazemieh.notes.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_note
import fintrack.core.designsystem.generated.resources.notes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesListScreen(
    viewModel: NotesViewModel = koinViewModel(),
    onAddNote: () -> Unit,
    onEditNote: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    FintrackScreen(
        title = stringResource(Res.string.notes),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.notes),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(NotesIntent.OnSearchQueryChanged(it)) },
                onAddClick = onAddNote,
                items = state.filteredNotes.map { note ->
                    EntityItem(
                        id = note.id,
                        name = note.title.ifEmpty { note.content.take(20) },
                        sub = if (note.title.isNotEmpty()) note.content else null,
                        badge = if (note.isPinned) "📌" else null,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onItemClick = { onEditNote(it.id) },
                onEditClick = { onEditNote(it.id) },
                onDeleteClick = { /* viewModel.onIntent(Delete) */ },
                showActions = true
            )
        }
    }
}
