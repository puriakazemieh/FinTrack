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
    minLine : Int = 1,
    maxLine : Int = if (singleLine) 1 else Int.MAX_VALUE,
    onClick: () -> Unit = {},
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        minLines = minLine,
        maxLines = maxLine,
        visualTransformation = if (isPrice) NumberCommaTransformation() else VisualTransformation.None,
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
        keyboardOptions = keyboardOptions,
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
        return TransformedText(
            text = AnnotatedString(text.text.toPrice()),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return text.text.toPrice().length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return text.length
                }
            }
        )
    }
}

