package com.kazemieh.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.displayIcon
import com.kazemieh.designsystem.displayLabel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.manage_tools_sub
import fintrack.core.designsystem.generated.resources.manage_tools_title
import fintrack.core.designsystem.generated.resources.navigation_tools
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ManageToolsScreen(
    onBack: () -> Unit,
    viewModel: ManageToolsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    FintrackScreen(
        title = stringResource(Res.string.manage_tools_title),
        sub = stringResource(Res.string.manage_tools_sub),
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                WidgetCard(title = stringResource(Res.string.navigation_tools)) {
                    ToolFeature.entries.forEach { feature ->
                        SettingItem(
                            title = feature.displayLabel(),
                            icon = feature.displayIcon,
                            on = feature !in state.disabledTools,
                            onToggle = { viewModel.onIntent(ManageToolsIntent.ToggleTool(feature)) }
                        )
                    }
                }
            }
        }
    }
}
