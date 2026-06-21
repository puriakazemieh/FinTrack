package com.kazemieh.notes.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Note
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.Fab
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.add_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesListScreen(
    viewModel: NotesViewModel= koinViewModel(),
    onAddNote: () -> Unit,
    onEditNote: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    FintrackScreen(
        title = stringResource(Res.string.notes),
        onBack = onBack,
        floatingActionButton = {
            Fab(
                label = stringResource(Res.string.add_note),
                icon = rememberVectorPainter(Icons.Default.Add),
                onClick = onAddNote,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(state.notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { onEditNote(note.id) }
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        tone = com.kazemieh.designsystem.component.glass.GlassTone.Default // Use tone instead of containerColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (note.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (note.title.isNotEmpty()) {
                FintrackTitleMediumText(
                    text = note.title,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.isLocked) {
                Icon(Icons.Default.Lock, contentDescription = null)
            } else {
                FintrackBodySmallText(
                    text = note.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 10
                )
            }
        }
    }
}
