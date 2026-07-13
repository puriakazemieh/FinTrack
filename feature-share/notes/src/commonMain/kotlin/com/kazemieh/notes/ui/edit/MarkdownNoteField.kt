package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glassTextFieldColors

/**
 * A lightweight markdown editor for note bodies: a formatting toolbar that inserts markdown syntax
 * at the caret (wrapping the selection for inline styles, or prefixing the current line for block
 * styles) plus a multi-line text field. The caller keeps a plain String; selection is tracked
 * locally so the toolbar can edit around the cursor.
 */
@Composable
fun MarkdownNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    var previewMode by remember { mutableStateOf(false) }
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

    fun wrapSelection(marker: String) {
        val text = fieldValue.text
        val start = fieldValue.selection.min
        val end = fieldValue.selection.max
        val newText = text.substring(0, start) + marker + text.substring(start, end) + marker + text.substring(end)
        val caret = if (start == end) start + marker.length else end + marker.length * 2
        apply(TextFieldValue(newText, TextRange(caret)))
    }

    fun prefixLine(prefix: String) {
        val text = fieldValue.text
        val caret = fieldValue.selection.min
        val lineStart = if (caret == 0) 0 else text.lastIndexOf('\n', caret - 1).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        apply(TextFieldValue(newText, TextRange(caret + prefix.length)))
    }

    GlassCard(padding = 0.dp, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ToolbarButton(Icons.Default.Title) { prefixLine("# ") }
                    ToolbarButton(Icons.Default.FormatBold) { wrapSelection("**") }
                    ToolbarButton(Icons.Default.FormatItalic) { wrapSelection("*") }
                    ToolbarButton(Icons.Default.FormatStrikethrough) { wrapSelection("~~") }
                    ToolbarButton(Icons.Default.CheckBox) { prefixLine("- [ ] ") }
                    ToolbarButton(Icons.Default.FormatListBulleted) { prefixLine("- ") }
                    ToolbarButton(Icons.Default.FormatListNumbered) { prefixLine("1. ") }
                }
                // Toggle between raw editing and the rendered ("display") view.
                ToolbarButton(
                    icon = if (previewMode) Icons.Default.Edit else Icons.Default.Visibility,
                    tint = if (previewMode) GlassGreen else glassColors.text2
                ) { previewMode = !previewMode }
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
