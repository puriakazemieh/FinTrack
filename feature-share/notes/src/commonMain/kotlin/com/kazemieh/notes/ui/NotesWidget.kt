package com.kazemieh.notes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Note
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import com.kazemieh.notes.ui.edit.MarkdownRenderer
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.notes_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesWidget(
    modifier: Modifier = Modifier,
    onMore: () -> Unit,
    onAdd: () -> Unit = {},
    onEditNote: (Note) -> Unit = {},
    viewModel: NotesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pinnedNotes = state.notes.filter { it.isPinned }
    // Pinned notes lead; fall back to the most recent when nothing is pinned.
    val displayNotes = if (pinnedNotes.isNotEmpty()) pinnedNotes else state.notes

    WidgetCard(
        title = stringResource(Res.string.notes),
        count = state.notes.size.takeIf { it > 0 },
        onMore = onMore,
        onAdd = onAdd,
        modifier = modifier
    ) {
        if (state.notes.isEmpty()) {
            WidgetEmptyState(
                icon = Icons.AutoMirrored.Filled.Note,
                text = stringResource(Res.string.notes_empty)
            )
        } else {
            Column {
                displayNotes.take(2).forEach { note ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditNote(note) }
                            .padding(vertical = 6.dp)
                    ) {
                        if (note.title.isNotEmpty()) {
                            FintrackTitleSmallText(
                                text = note.title,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        MarkdownRenderer(
                            text = if (note.isLocked) "********" else note.content,
                            onToggleCheckbox = {},
                            interactive = false
                        )
                    }
                }
            }
        }
    }
}
