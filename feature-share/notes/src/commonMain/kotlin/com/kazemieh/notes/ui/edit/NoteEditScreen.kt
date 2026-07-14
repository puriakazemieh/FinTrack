package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Tag
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.ColorSwatches
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.RemovableChip
import com.kazemieh.designsystem.component.glass.SectionContainer
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_add_tag
import fintrack.core.designsystem.generated.resources.edit_note
import fintrack.core.designsystem.generated.resources.label_optional
import fintrack.core.designsystem.generated.resources.label_tag_prefix
import fintrack.core.designsystem.generated.resources.note_color_label
import fintrack.core.designsystem.generated.resources.note_content_hint
import fintrack.core.designsystem.generated.resources.note_text_label
import fintrack.core.designsystem.generated.resources.note_title_hint
import fintrack.core.designsystem.generated.resources.reminder
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.tags
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteEditScreen(
    viewModel: NoteEditViewModel = koinViewModel(),
    noteId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showTagSheet by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    // Carries the picked reminder date between the date sheet and the time sheet.
    var pendingReminderDate by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(noteId) {
        viewModel.onIntent(NoteEditIntent.LoadNote(noteId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                NoteEditEffect.SaveSuccess -> onBack()
                is NoteEditEffect.ShowError -> {}
            }
        }
    }

    val pickerColors = FinTrackPickerColors.rainbow()
    val swatchColors = pickerColors.map { it.color }
    val optional = stringResource(Res.string.label_optional)
    val tz = TimeZone.currentSystemDefault()

    FintrackScreen(
        title = stringResource(Res.string.edit_note),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pin / Lock actions, given a clear home at the top of the form.
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToggleButton(
                        icon = Icons.Default.PushPin,
                        active = state.isPinned,
                        onClick = { viewModel.onIntent(NoteEditIntent.OnTogglePin) }
                    )
                    ToggleButton(
                        icon = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        active = state.isLocked,
                        onClick = { viewModel.onIntent(NoteEditIntent.OnToggleLock) }
                    )
                }

                // Title — the Field itself renders the "(optional)" marker.
                Field(label = stringResource(Res.string.note_title_hint)) {
                    InlineTextField(
                        value = state.title,
                        onValueChange = { viewModel.onIntent(NoteEditIntent.OnTitleChanged(it)) },
                        placeholder = ""
                    )
                }

                // Tags (optional) — same section pattern as add-transaction
                SectionContainer(
                    title = "${stringResource(Res.string.tags)} ($optional)",
                    onAddClick = { showTagSheet = true },
                    addLabel = stringResource(Res.string.btn_add_tag)
                ) {
                    state.selectedTags.forEach { tag ->
                        val color = pickerColors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                        RemovableChip(
                            label = stringResource(Res.string.label_tag_prefix, tag.name),
                            color = color,
                            onRemove = {
                                viewModel.onIntent(NoteEditIntent.OnTagsChanged(state.selectedTags.filter { it.id != tag.id }))
                            }
                        )
                    }
                }

                // Note text (optional)
                FintrackLabelMediumText(text = "${stringResource(Res.string.note_text_label)} ($optional)")
                MarkdownNoteField(
                    value = state.content,
                    onValueChange = { viewModel.onIntent(NoteEditIntent.OnContentChanged(it)) },
                    placeholder = stringResource(Res.string.note_content_hint),
                    modifier = Modifier.fillMaxWidth(),
                    startInPreview = noteId != 0L
                )

                // Reminder — the Field renders the "(optional)" marker.
                Field(
                    label = stringResource(Res.string.reminder),
                    onClick = { showDatePicker.value = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (state.reminderTime != null) GlassGreen else LocalGlassColors.current.text2
                        )
                        FintrackBodyMediumText(
                            text = state.reminderTime?.let { DateUtils.formatTimestamp(it) } ?: stringResource(Res.string.reminder),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Color (optional) — kept at the bottom as requested
                FintrackLabelMediumText(text = "${stringResource(Res.string.note_color_label)} ($optional)")
                ColorSwatches(
                    colors = swatchColors,
                    pickedIndex = swatchColors.indexOfFirst { it.value.toLong() == state.color },
                    onColorPick = { viewModel.onIntent(NoteEditIntent.OnColorChanged(swatchColors[it].value.toLong())) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Fixed save button at the bottom, like the add-transaction sheet.
            FintrackButton(
                text = stringResource(Res.string.save_),
                onClick = { viewModel.onIntent(NoteEditIntent.OnSave) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        if (showTagSheet) {
            TagPickerBottomSheet(
                selectedTags = state.selectedTags.toSet(),
                onSubmitClick = { tags ->
                    viewModel.onIntent(NoteEditIntent.OnTagsChanged(tags?.toList() ?: emptyList()))
                    showTagSheet = false
                },
                onDismiss = { showTagSheet = false }
            )
        }

        // Reminder: pick a date, then a time, then combine both into the reminder timestamp.
        JalaliDatePickerBottomSheet(
            openSheet = showDatePicker,
            onConfirm = { calendar ->
                pendingReminderDate = calendar.toTimestamp()
                showDatePicker.value = false
                showTimePicker.value = true
            }
        )

        val initialTime = state.reminderTime?.let {
            val dt = Instant.fromEpochMilliseconds(it).toLocalDateTime(tz)
            "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
        } ?: "09:00"
        FintrackTimePickerBottomSheet(
            openSheet = showTimePicker,
            initialTime = initialTime,
            onConfirm = { time ->
                val parts = time.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val base = pendingReminderDate ?: state.reminderTime
                if (base != null) {
                    val combined = base + hour * 3_600_000L + minute * 60_000L
                    viewModel.onIntent(NoteEditIntent.OnReminderChanged(combined))
                }
            }
        )
    }
}

@Composable
private fun ToggleButton(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else glassColors.glass)
            .border(1.dp, if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else glassColors.glassEdge, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary else glassColors.text2,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun InlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val glassColors = LocalGlassColors.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = glassColors.text),
        cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    FintrackBodyMediumText(text = placeholder, color = glassColors.text3)
                }
                innerTextField()
            }
        }
    )
}
