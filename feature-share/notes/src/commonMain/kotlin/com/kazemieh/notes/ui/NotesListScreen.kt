package com.kazemieh.notes.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import com.kazemieh.common.model.Note
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.Fab
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.notes.ui.NotesViewModel
import com.kazemieh.notes.ui.NotesIntent
import com.kazemieh.notes.ui.edit.MarkdownRenderer
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.hint_search_in
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
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    FintrackScreen(
        title = stringResource(Res.string.notes),
        onBack = onBack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onIntent(NotesIntent.OnSearchQueryChanged(it)) },
                    placeholder = stringResource(Res.string.hint_search_in, stringResource(Res.string.notes)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onEditNote(note.id) },
                            onLongClick = { noteToDelete = note },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Fab(
                label = stringResource(Res.string.add_note),
                icon = rememberVectorPainter(Icons.Default.Add),
                onClick = onAddNote,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            )
        }
    }

    noteToDelete?.let { note ->
        DeleteBottomSheet(
            itemName = note.title.ifEmpty { note.content.take(20) },
            dismissClicked = { noteToDelete = null },
            confirmClicked = {
                viewModel.onIntent(NotesIntent.OnDeleteNote(note.id))
                noteToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    // Handling both ARGB (lower 32 bits) and Packed ULong (upper 32 bits) for backward compatibility
    val accentColor = if (note.color != 0L) {
        val colorULong = note.color.toULong()
        if (colorULong <= 0xFFFFFFFFuL) {
            Color(note.color.toInt())
        } else {
            Color(colorULong)
        }
    } else {
        glassColors.text3
    }

    // Consistent with the app-wide dark glass surfaces: a low-alpha tint of the
    // chosen accent over the dark background, with light glass text on top.
    val onCardColor = glassColors.text

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(0.5.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.title.isNotEmpty()) {
                    FintrackTitleSmallText(
                        text = note.title,
                        color = onCardColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = onCardColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (note.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = onCardColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (note.title.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))

            if (note.isLocked) {
                FintrackBodyMediumText(
                    text = "••••••••",
                    color = onCardColor.copy(alpha = 0.8f),
                    maxLines = 1
                )
            } else {
                // Render the note body as formatted markdown instead of raw syntax.
                MarkdownRenderer(
                    text = note.content,
                    onToggleCheckbox = {},
                    interactive = false,
                    textColor = onCardColor.copy(alpha = 0.85f)
                )
            }

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(onCardColor.copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            FintrackLabelSmallText(
                                text = "#${tag.name}",
                                color = onCardColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
