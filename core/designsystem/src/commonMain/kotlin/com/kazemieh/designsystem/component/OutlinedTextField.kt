package com.kazemieh.designsystem.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import com.kazemieh.common.toFa
import com.kazemieh.common.toPrice
import com.kazemieh.designsystem.LocalSpacing

@Composable
fun FintrackOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: @Composable (() -> Unit),
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    containerColor: Color = MaterialTheme.colorScheme.background,
    disabledBorderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    disabledContainerColor: Color = MaterialTheme.colorScheme.background,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    cursorColor: Color = MaterialTheme.colorScheme.primary,
    errorColor: Color = MaterialTheme.colorScheme.error,
    shape: Shape = RoundedCornerShape(LocalSpacing.current.mediumLarge),
    enabled: Boolean = true,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    isPrice: Boolean = false,
    isPersianNumber: Boolean = false,
    isFourDigitGroup: Boolean = false,
    minLine : Int = 1,
    maxLine : Int = if (singleLine) 1 else Int.MAX_VALUE,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {

    val visualTransformation = when {
        isPrice -> NumberCommaTransformation()
        isPersianNumber -> PersianNumberTransformation()
        isFourDigitGroup -> FourDigitGroupingTransformation()
        else -> VisualTransformation.None
    }

    val finalKeyboardOptions = if (isPrice || isPersianNumber || isFourDigitGroup) {
        keyboardOptions.copy(keyboardType = KeyboardType.Number)
    } else {
        keyboardOptions
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        minLines = minLine,
        maxLines = maxLine,
        visualTransformation = visualTransformation,
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        shape = shape,
        textStyle = textStyle.copy(color = textColor),
        label = {
            label()
        },
        enabled = enabled,
        singleLine = singleLine,
        readOnly = readOnly,
        isError = isError,
        keyboardOptions = finalKeyboardOptions,
        prefix = prefix,
        suffix = suffix,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledBorderColor = if (isError) errorColor else disabledBorderColor,
            disabledContainerColor = disabledContainerColor,
            cursorColor = cursorColor
        )
    )


}

class NumberCommaTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val transformedText = originalText.toPrice().toFa()
        
        return TransformedText(
            text = AnnotatedString(transformedText),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return 0
                    if (offset >= originalText.length) return transformedText.length
                    
                    var result = offset
                    for (i in 1 until originalText.length) {
                        if ((originalText.length - i) % 3 == 0) {
                            if (i < offset) result++
                        }
                    }
                    return result.coerceIn(0, transformedText.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 0) return 0
                    if (offset >= transformedText.length) return originalText.length
                    
                    var commasBefore = 0
                    for (i in 0 until offset) {
                        if (i < transformedText.length && transformedText[i] == ',') {
                            commasBefore++
                        }
                    }
                    return (offset - commasBefore).coerceIn(0, originalText.length)
                }
            }
        )
    }
}

class PersianNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformedText = text.text.toFa()
        return TransformedText(
            text = AnnotatedString(transformedText),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

class FourDigitGroupingTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val transformedText = originalText.chunked(4).joinToString("-").toFa()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val dashes = (offset - 1) / 4
                return (offset + dashes).coerceAtMost(transformedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val dashes = offset / 5
                return (offset - dashes).coerceAtMost(originalText.length)
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}

