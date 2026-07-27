package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.Tag
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
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

    FintrackScreen(
        onBack = onBack
    ) {
        NoteEditContent(
            viewModel = viewModel,
            onBack = onBack,
            noteId = noteId
        )
    }
}

@Composable
fun NoteEditContent(
    viewModel: NoteEditViewModel,
    onBack: () -> Unit,
    noteId: Long
) {
    val state by viewModel.state.collectAsState()
    var showTagSheet by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    var pendingReminderDate by remember { mutableStateOf<Long?>(null) }

    val pickerColors = FinTrackPickerColors.rainbow()
    val swatchColors = pickerColors.map { it.color }
    val optional = stringResource(Res.string.label_optional)
    val tz = TimeZone.currentSystemDefault()

    AddFrame(
        title = if (noteId == 0L) stringResource(Res.string.add_note) else stringResource(Res.string.edit_note),
        primaryLabel = stringResource(Res.string.save_),
        onPrimaryClick = { viewModel.onIntent(NoteEditIntent.OnSave) },
        onClose = onBack,
        showHero = false,
        preventSwipeDismiss = false,
        horizontalPadding = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TitledField(
                label = stringResource(Res.string.label_title),
                value = state.title,
                onValueChange = { viewModel.onIntent(NoteEditIntent.OnTitleChanged(it)) },
                placeholder = stringResource(Res.string.note_title_hint)
            )

            MarkdownNoteField(
                value = state.content,
                onValueChange = { viewModel.onIntent(NoteEditIntent.OnContentChanged(it)) },
                placeholder = stringResource(Res.string.note_content_hint),
                modifier = Modifier.fillMaxWidth()
            )

            // Color selection with Label on top
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FintrackLabelMediumText(text = stringResource(Res.string.note_color_label))
                ColorSwatches(
                    colors = swatchColors,
                    pickedIndex = swatchColors.indexOfFirst { it.value.toLong() == state.color },
                    onColorPick = { viewModel.onIntent(NoteEditIntent.OnColorChanged(swatchColors[it].value.toLong())) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Pin and Lock in a single linear block with labels on top
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FintrackLabelMediumText(text = stringResource(Res.string.label_pin))
                    ToggleButton(
                        icon = Icons.Default.PushPin,
                        active = state.isPinned,
                        onClick = { viewModel.onIntent(NoteEditIntent.OnTogglePin) },
                        text = if (state.isPinned) stringResource(Res.string.label_pin_active) else stringResource(Res.string.label_pin_inactive),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FintrackLabelMediumText(text = stringResource(Res.string.label_lock))
                    ToggleButton(
                        icon = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        active = state.isLocked,
                        onClick = { viewModel.onIntent(NoteEditIntent.OnToggleLock) },
                        text = if (state.isLocked) stringResource(Res.string.label_lock_active) else stringResource(Res.string.label_lock_inactive),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SectionContainer(
                title = stringResource(Res.string.tags),
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
            
            Spacer(Modifier.height(120.dp))
        }
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

    val openDatePicker = remember { mutableStateOf(false) }
    LaunchedEffect(showDatePicker.value) { if(showDatePicker.value) openDatePicker.value = true }
    JalaliDatePickerBottomSheet(
        openSheet = openDatePicker,
        onConfirm = { calendar ->
            pendingReminderDate = calendar.toTimestamp()
            showDatePicker.value = false
            showTimePicker.value = true
        }
    )

    val openTimePicker = remember { mutableStateOf(false) }
    LaunchedEffect(showTimePicker.value) { if(showTimePicker.value) openTimePicker.value = true }
    val initialTime = state.reminderTime?.let {
        val dt = Instant.fromEpochMilliseconds(it).toLocalDateTime(tz)
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    } ?: "09:00"
    FintrackTimePickerBottomSheet(
        openSheet = openTimePicker,
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
            showTimePicker.value = false
        }
    )
}

@Composable
private fun ToggleButton(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    text: String? = null,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else glassColors.glass)
            .border(1.dp, if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else glassColors.glassEdge, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else glassColors.text2,
                modifier = Modifier.size(20.dp)
            )
            if (text != null) {
                FintrackLabelSmallText(
                    text = text,
                    color = if (active) MaterialTheme.colorScheme.primary else glassColors.text
                )
            }
        }
    }
}

@Composable
private fun TitledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FintrackLabelSmallText(text = label, color = glassColors.text3)
            InlineTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder
            )
        }
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
        cursorBrush = Brush.verticalGradient(listOf(com.kazemieh.designsystem.GlassGreen, com.kazemieh.designsystem.GlassGreen)),
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
