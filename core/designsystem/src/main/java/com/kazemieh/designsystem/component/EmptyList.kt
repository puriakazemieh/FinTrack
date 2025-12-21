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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R

@Composable
fun EmptyListScreen(title: String = stringResource(R.string.empty_title)) {
    val space = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(space.huge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, "", modifier = Modifier.size(space.huge))
        Spacer(modifier = Modifier.height(space.mediumSmall))
        FintrackHeadlineSmallText(title, textAlign = TextAlign.Center)
    }
}