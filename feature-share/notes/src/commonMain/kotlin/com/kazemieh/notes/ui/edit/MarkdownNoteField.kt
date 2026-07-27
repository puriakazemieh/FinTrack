package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassColors
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarkdownNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val density = LocalDensity.current
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

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
            val newText = text.substring(0, lineStart) + currentLine.removePrefix(prefix) + text.substring(lineEnd)
            apply(TextFieldValue(newText, TextRange((caret - prefix.length).coerceAtLeast(lineStart))))
        } else {
            val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
            apply(TextFieldValue(newText, TextRange(caret + prefix.length)))
        }
    }

    // Unified Card for Toolbar and Text
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, MaterialTheme.shapes.medium)
    ) {
        // Toolbar
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
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

        HorizontalDivider(color = glassColors.glassHairline, thickness = 1.dp)

        // Text Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .pointerInput(fieldValue.text) {
                    detectTapGestures { offset ->
                        layoutResult?.let { layout ->
                            val lineIndex = layout.getLineForVerticalPosition(offset.y)
                            if (lineIndex >= layout.lineCount) return@detectTapGestures
                            val lineStart = layout.getLineStart(lineIndex)
                            val lineEnd = layout.getLineEnd(lineIndex)
                            if (lineStart >= fieldValue.text.length) return@detectTapGestures
                            val lineText = fieldValue.text.substring(lineStart, lineEnd)

                            // Checkbox click area for RTL (Right side)
                            // In RTL, Start is Right. offset.x < 48dp would be the right edge.
                            if (offset.x < with(density) { 48.dp.toPx() }) {
                                if (lineText.trimStart().startsWith("- [ ]") || lineText.trimStart().startsWith("- [x]")) {
                                    apply(
                                        TextFieldValue(
                                            toggleCheckboxLine(fieldValue.text, lineIndex),
                                            fieldValue.selection
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
        ) {
            BasicTextField(
                value = fieldValue,
                onValueChange = { apply(it) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = glassColors.text,
                    textAlign = TextAlign.Start // This is Right in RTL
                ),
                cursorBrush = Brush.verticalGradient(listOf(com.kazemieh.designsystem.GlassGreen, com.kazemieh.designsystem.GlassGreen)),
                onTextLayout = { layoutResult = it },
                visualTransformation = MarkdownVisualTransformation(glassColors),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp, end = 12.dp, top = 12.dp, bottom = 12.dp) // Start is Right in RTL
                    .heightIn(min = 180.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (fieldValue.text.isEmpty()) {
                            FintrackBodyMediumText(text = placeholder, color = glassColors.text3)
                        }

                        layoutResult?.let { layout ->
                            val lines = fieldValue.text.split("\n")
                            lines.forEachIndexed { i, line ->
                                if (i >= layout.lineCount) return@forEachIndexed
                                val top = layout.getLineTop(i)

                                if (line.trimStart().startsWith("- [ ]") || line.trimStart().startsWith("- [x]")) {
                                    val isChecked = line.trimStart().startsWith("- [x]")
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart) // Top Right in RTL
                                            .offset(x = (-32).dp, y = with(density) { top.toDp() })
                                            .size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CheckMark(checked = isChecked)
                                    }
                                } else if (line.trimStart().startsWith("- ")) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart) // Top Right in RTL
                                            .offset(x = (-32).dp, y = with(density) { top.toDp() })
                                            .size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FintrackBodyMediumText(text = "•", color = glassColors.text2)
                                    }
                                }
                            }
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun CheckMark(checked: Boolean) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (checked) com.kazemieh.designsystem.GlassGreen else Color.Transparent)
            .border(1.2.dp, if (checked) com.kazemieh.designsystem.GlassGreen else glassColors.glassEdge, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private class MarkdownVisualTransformation(private val glassColors: GlassColors) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            val lines = text.text.split("\n")
            lines.forEachIndexed { index, line ->
                when {
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 1.sp)) { append("# ") }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                            append(line.substring(2))
                        }
                    }
                    line.trimStart().startsWith("- [ ]") || line.trimStart().startsWith("- [x]") -> {
                        val marker = if (line.contains("- [ ]")) "- [ ]" else "- [x]"
                        val start = line.indexOf(marker)
                        append(line.substring(0, start))
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 1.sp)) { append(marker) }
                        append(line.substring(start + marker.length))
                    }
                    line.trimStart().startsWith("- ") -> {
                        val start = line.indexOf("- ")
                        append(line.substring(0, start))
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 1.sp)) { append("- ") }
                        append(line.substring(start + 2))
                    }
                    else -> append(line)
                }
                if (index < lines.size - 1) append("\n")
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    tint: Color = LocalGlassColors.current.text2,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
