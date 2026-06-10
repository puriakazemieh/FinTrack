package com.kazemieh.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.designsystem.LocalSpacing
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.empty_title
import fintrack.core.designsystem.generated.resources.empty_title_default
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyList(
    title: String? = null,
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current

    val displayTitle = title ?: stringResource(Res.string.empty_title_default)
    val text = stringResource(Res.string.empty_title, displayTitle)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(space.huge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, "", modifier = Modifier.size(space.huge))
        Spacer(modifier = Modifier.height(space.mediumSmall))
        FintrackHeadlineSmallText(text, textAlign = TextAlign.Center)
    }
}
