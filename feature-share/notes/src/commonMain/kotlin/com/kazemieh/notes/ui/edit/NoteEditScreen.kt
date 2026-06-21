package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.glass.ColorSwatches
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.Fab
import com.kazemieh.designsystem.component.glass.ItemSelected
import com.kazemieh.designsystem.component.bottomsheet.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.edit_note
import fintrack.core.designsystem.generated.resources.note_content_hint
import fintrack.core.designsystem.generated.resources.note_title_hint
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.reminder
import fintrack.core.designsystem.generated.resources.save_
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteEditScreen(
    viewModel: NoteEditViewModel= koinViewModel(),
    noteId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showTagSheet by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.onIntent(NoteEditIntent.LoadNote(noteId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                NoteEditEffect.SaveSuccess -> onBack()
                is NoteEditEffect.ShowError -> {
                }
            }
        }
    }

    val pickerColors = FinTrackPickerColors.rainbow().map { it.color }

    FintrackScreen(
        title = stringResource(Res.string.edit_note),
        onBack = onBack,
        trailingContent = {
            IconButton(onClick = { viewModel.onIntent(NoteEditIntent.OnTogglePin) }) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = if (state.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { viewModel.onIntent(NoteEditIntent.OnToggleLock) }) {
                Icon(
                    imageVector = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (state.isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        floatingActionButton = {
            Fab(
                label = stringResource(Res.string.save_),
                icon = rememberVectorPainter(Icons.Default.Save),
                onClick = { viewModel.onIntent(NoteEditIntent.OnSave) },
                modifier = Modifier.padding(16.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ColorSwatches(
                colors = pickerColors,
                pickedIndex = pickerColors.indexOfFirst { it.value.toLong() == state.color },
                onColorPick = { viewModel.onIntent(NoteEditIntent.OnColorChanged(pickerColors[it].value.toLong())) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Field(label = stringResource(Res.string.note_title_hint)) {
                FintrackOutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onIntent(NoteEditIntent.OnTitleChanged(it)) },
                    placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.note_title_hint)) },
                    label = { FintrackLabelSmallText(text = stringResource(Res.string.note_title_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ItemSelected(
                isSelected = state.selectedTags.isNotEmpty(),
                item = ItemUi(
                    id = 0,
                    title = UiText.DynamicString(if (state.selectedTags.isEmpty()) stringResource(Res.string.tags) else state.selectedTags.joinToString { it.name }),
                ),
                onToggle = { showTagSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Field(label = stringResource(Res.string.note_content_hint)) {
                FintrackOutlinedTextField(
                    value = state.content,
                    onValueChange = { viewModel.onIntent(NoteEditIntent.OnContentChanged(it)) },
                    placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.note_content_hint)) },
                    label = { FintrackLabelSmallText(text = stringResource(Res.string.note_content_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.padding(4.dp))
                FintrackBodyLargeText(text = stringResource(Res.string.reminder))
            }
        }

        if (showTagSheet) {
            SelectableFlowRowBottomSheet(
                title = stringResource(Res.string.tags),
                items = state.allTags.map { it.toItemUi() }.toSet(),
                initialSelection = state.selectedTags.map { it.toItemUi() }.toSet(),
                onConfirm = { items ->
                    val selectedIds = items.map { it.id }
                    val newList = state.allTags.filter { it.id in selectedIds }
                    viewModel.onIntent(NoteEditIntent.OnTagsChanged(newList))
                    showTagSheet = false
                },
                onDismiss = { showTagSheet = false },
                onAddClick = { /* Handle add tag */ }
            )
        }
    }
}

private fun com.kazemieh.common.model.Tag.toItemUi(): ItemUi {
    return ItemUi(
        id = id ?: 0L,
        title = UiText.DynamicString(name),
        iconId = iconId,
        colorId = colorId
    )
}
