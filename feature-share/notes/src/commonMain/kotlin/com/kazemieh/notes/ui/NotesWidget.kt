package com.kazemieh.notes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.notes_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesWidget(
    modifier: Modifier = Modifier,
    onMore: () -> Unit,
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
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        if (note.title.isNotEmpty()) {
                            FintrackTitleSmallText(
                                text = note.title,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FintrackBodySmallText(
                            text = if (note.isLocked) "********" else note.content,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
