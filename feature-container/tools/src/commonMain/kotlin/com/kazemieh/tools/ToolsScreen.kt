package com.kazemieh.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.glass.ScreenHeader
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_tools_coming_soon
import fintrack.core.designsystem.generated.resources.navigation_tools
import fintrack.core.designsystem.generated.resources.title_tools_management
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToolsScreen(
    snackbarHostState: SnackbarHostState
) {
    val space = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(Res.string.navigation_tools),
            sub = stringResource(Res.string.title_tools_management)
        )

        FintrackBodyLargeText(
            text = stringResource(Res.string.msg_tools_coming_soon),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
