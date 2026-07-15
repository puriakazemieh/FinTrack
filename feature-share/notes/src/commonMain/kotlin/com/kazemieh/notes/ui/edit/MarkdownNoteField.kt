package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glassTextFieldColors
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.label_preview
import org.jetbrains.compose.resources.stringResource

/**
 * A lightweight markdown editor for note bodies: a formatting toolbar that inserts markdown syntax
 * at the caret (wrapping the selection for inline styles, or prefixing the current line for block
 * styles) plus a multi-line text field. The caller keeps a plain String; selection is tracked
 * locally so the toolbar can edit around the cursor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarkdownNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    startInPreview: Boolean = false
) {
    val glassColors = LocalGlassColors.current
    var previewMode by remember { mutableStateOf(startInPreview) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    // Keep in sync when the note is (re)loaded from the ViewModel.
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    fun apply(newValue: TextFieldValue) {
        fieldValue = newValue
        onValueChange(newValue.text)
    }

    // Toggling: if the selection is already wrapped by the marker (just outside or just inside the
    // selection) remove it, otherwise add it. Fixes repeated taps stacking "****" markers.
    fun wrapSelection(marker: String) {
        val text = fieldValue.text
        val start = fieldValue.selection.min
        val end = fieldValue.selection.max
        val len = marker.length

        val markersOutside = start >= len && end + len <= text.length &&
            text.substring(start - len, start) == marker &&
            text.substring(end, end + len) == marker
        if (markersOutside) {
            val newText = text.substring(0, start - len) + text.substring(start, end) + text.substring(end + len)
            apply(TextFieldValue(newText, TextRange(start - len, end - len)))
            return
        }

        val selected = text.substring(start, end)
        if (selected.length >= 2 * len && selected.startsWith(marker) && selected.endsWith(marker)) {
            val inner = selected.substring(len, selected.length - len)
            val newText = text.substring(0, start) + inner + text.substring(end)
            apply(TextFieldValue(newText, TextRange(start, start + inner.length)))
            return
        }

        val newText = text.substring(0, start) + marker + selected + marker + text.substring(end)
        val selection = if (start == end) TextRange(start + len) else TextRange(start + len, end + len)
        apply(TextFieldValue(newText, selection))
    }

    fun prefixLine(prefix: String) {
        val text = fieldValue.text
        val caret = fieldValue.selection.min
        val lineStart = if (caret == 0) 0 else text.lastIndexOf('\n', caret - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
        val currentLine = text.substring(lineStart, lineEnd)
        if (currentLine.startsWith(prefix)) {
            // Remove an already-applied block marker instead of stacking another one.
            val newText = text.substring(0, lineStart) + currentLine.removePrefix(prefix) + text.substring(lineEnd)
            apply(TextFieldValue(newText, TextRange((caret - prefix.length).coerceAtLeast(lineStart))))
        } else {
            val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
            apply(TextFieldValue(newText, TextRange(caret + prefix.length)))
        }
    }

    GlassCard(padding = 0.dp, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Format buttons wrap onto extra lines instead of hiding behind a horizontal
                // scroll, so every action stays visible on narrow screens.
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Using a formatting action while previewing drops back to edit and applies it.
                    ToolbarButton(Icons.Default.Title) { previewMode = false; prefixLine("# ") }
                    ToolbarButton(Icons.Default.FormatBold) { previewMode = false; wrapSelection("**") }
                    ToolbarButton(Icons.Default.FormatItalic) { previewMode = false; wrapSelection("*") }
                    ToolbarButton(Icons.Default.FormatStrikethrough) { previewMode = false; wrapSelection("~~") }
                    ToolbarButton(Icons.Default.CheckBox) { previewMode = false; prefixLine("- [ ] ") }
                    ToolbarButton(Icons.Default.FormatListBulleted) { previewMode = false; prefixLine("- ") }
                    ToolbarButton(Icons.Default.FormatListNumbered) { previewMode = false; prefixLine("1. ") }
                }
                // Clear labelled toggle between raw editing and the rendered ("display") view.
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (previewMode) GlassGreen.copy(alpha = 0.15f) else glassColors.glass)
                        .clickable { previewMode = !previewMode }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (previewMode) Icons.Default.Edit else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (previewMode) GlassGreen else glassColors.text2,
                        modifier = Modifier.size(16.dp)
                    )
                    FintrackLabelSmallText(
                        text = if (previewMode) stringResource(Res.string.edit) else stringResource(Res.string.label_preview),
                        color = if (previewMode) GlassGreen else glassColors.text2
                    )
                }
            }

            HorizontalDivider(color = glassColors.glassHairline)

            if (previewMode) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp).padding(12.dp)) {
                    if (fieldValue.text.isBlank()) {
                        FintrackBodyMediumText(text = placeholder, color = glassColors.text3)
                    } else {
                        MarkdownRenderer(
                            text = fieldValue.text,
                            onToggleCheckbox = { lineIndex ->
                                apply(
                                    TextFieldValue(
                                        toggleCheckboxLine(fieldValue.text, lineIndex),
                                        TextRange(fieldValue.selection.min)
                                    )
                                )
                            }
                        )
                    }
                }
            } else {
                TextField(
                    value = fieldValue,
                    onValueChange = { apply(it) },
                    placeholder = { FintrackBodyMediumText(text = placeholder, color = glassColors.text3) },
                    colors = glassTextFieldColors(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = glassColors.text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp),
                    shape = RoundedCornerShape(0.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color = LocalGlassColors.current.text2,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
