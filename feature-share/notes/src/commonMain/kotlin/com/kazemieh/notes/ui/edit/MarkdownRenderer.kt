package com.kazemieh.notes.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText

/**
 * Renders the markdown a note body uses (headings, bold/italic/strikethrough, bullet and
 * numbered lists, and task checkboxes) as styled text instead of raw syntax. Task checkboxes
 * are interactive: tapping one calls [onToggleCheckbox] with the source line index so the
 * caller can flip "- [ ]" <-> "- [x]".
 */
@Composable
fun MarkdownRenderer(
    text: String,
    onToggleCheckbox: (lineIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    textColor: Color? = null
) {
    val glassColors = LocalGlassColors.current
    val baseColor = textColor ?: glassColors.text
    val mutedColor = textColor?.copy(alpha = 0.6f) ?: glassColors.text3
    val lines = text.split("\n")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (text.isBlank()) return@Column
        lines.forEachIndexed { index, raw ->
            val line = raw.trimEnd()
            when {
                CHECKBOX_UNCHECKED.containsMatchIn(line) || CHECKBOX_CHECKED.containsMatchIn(line) -> {
                    val checked = CHECKBOX_CHECKED.containsMatchIn(line)
                    val content = line.replaceFirst(CHECKBOX_ANY, "").trimStart()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (interactive) Modifier.clickable { onToggleCheckbox(index) } else Modifier),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CheckMark(checked = checked)
                        Text(
                            text = inline(content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked) mutedColor else baseColor
                        )
                    }
                }

                line.startsWith("# ") -> {
                    Text(
                        text = inline(line.removePrefix("# ")),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = baseColor
                    )
                }

                line.startsWith("- ") -> {
                    BulletLine(bullet = "•", content = inline(line.removePrefix("- ")), color = baseColor)
                }

                NUMBERED.matchEntire(line) != null -> {
                    val match = NUMBERED.matchEntire(line)!!
                    BulletLine(bullet = "${match.groupValues[1]}.", content = inline(match.groupValues[2]), color = baseColor)
                }

                line.isBlank() -> Spacer(Modifier.size(6.dp))

                else -> {
                    Text(
                        text = inline(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = baseColor
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletLine(bullet: String, content: AnnotatedString, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FintrackBodyMediumText(text = bullet, color = color)
        Text(text = content, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun CheckMark(checked: Boolean) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) GlassGreen else Color.Transparent)
            .border(1.5.dp, if (checked) GlassGreen else glassColors.glassEdge, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private val CHECKBOX_UNCHECKED = Regex("""^\s*-\s*\[\s\]""")
private val CHECKBOX_CHECKED = Regex("""^\s*-\s*\[[xX]\]""")
private val CHECKBOX_ANY = Regex("""^\s*-\s*\[[ xX]\]""")
private val NUMBERED = Regex("""^(\d+)\.\s+(.*)""")

/** Flip "- [ ]" <-> "- [x]" on the given source line; returns the updated full text. */
fun toggleCheckboxLine(text: String, lineIndex: Int): String {
    val lines = text.split("\n").toMutableList()
    if (lineIndex !in lines.indices) return text
    val line = lines[lineIndex]
    lines[lineIndex] = when {
        CHECKBOX_UNCHECKED.containsMatchIn(line) -> line.replaceFirst("[ ]", "[x]")
        CHECKBOX_CHECKED.containsMatchIn(line) -> line.replaceFirst(Regex("""\[[xX]\]"""), "[ ]")
        else -> line
    }
    return lines.joinToString("\n")
}

/** Parse inline markdown (**bold**, *italic*, ~~strike~~) into a styled AnnotatedString. */
private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    append(text[i]); i++
                }
            }

            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    append(text[i]); i++
                }
            }

            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    append(text[i]); i++
                }
            }

            else -> {
                append(text[i]); i++
            }
        }
    }
}
