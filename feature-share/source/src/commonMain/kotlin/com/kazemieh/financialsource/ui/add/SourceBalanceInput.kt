package com.kazemieh.financialsource.ui.add

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.kazemieh.common.toEnglishDigits
import com.kazemieh.designsystem.component.NumberCommaTransformation

/** A lone sign is a valid editing state, separate from the numeric domain value. */
internal fun parseSourceBalance(text: String): Long? {
    val normalized = text.toEnglishDigits().replace('−', '-')
    return when (normalized) {
        "", "-", "+" -> 0L
        else -> normalized.toLongOrNull()
    }
}

/** Reuse the shared grouping while keeping the sign outside the digit groups. */
internal class SourceBalanceTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val signLength = if (text.text.firstOrNull() in listOf('-', '+', '−')) 1 else 0
        val digits = NumberCommaTransformation().filter(AnnotatedString(text.text.drop(signLength)))
        val sign = text.text.take(signLength).replace('−', '-')
        return TransformedText(
            AnnotatedString(sign + digits.text.text),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    if (offset <= signLength) offset
                    else signLength + digits.offsetMapping.originalToTransformed(offset - signLength)

                override fun transformedToOriginal(offset: Int): Int =
                    if (offset <= signLength) offset
                    else signLength + digits.offsetMapping.transformedToOriginal(offset - signLength)
            }
        )
    }
}
