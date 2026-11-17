package com.kazemieh.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.kazemieh.designsystem.LocalSpacing

/** Persistent feedback inside the active sheet, next to the action or record it describes. */
@Composable
fun FintrackInlineFeedback(text: String, modifier: Modifier = Modifier, isError: Boolean = false,
    announce: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().then(if (announce) Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        } else Modifier),
        shape = MaterialTheme.shapes.medium,
        color = if (isError) colors.errorContainer else colors.surfaceVariant
    ) {
        FintrackBodyMediumText(text = text, modifier = Modifier.padding(LocalSpacing.current.mediumLarge),
            color = if (isError) colors.onErrorContainer else colors.onSurfaceVariant)
    }
}
