package com.kazemieh.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.designsystem.LocalSpacing


@Composable
fun FilterButton(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(space.mediumLarge)
    ) {
        FintrackBodyLargeText(text, textAlign = textAlign)
    }
}