package com.kazemieh.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.displayIcon
import com.kazemieh.designsystem.displayLabel
import com.kazemieh.notifications.ui.SmsPermissionLauncher
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.capability_sms_title
import fintrack.core.designsystem.generated.resources.manage_capabilities_title
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

    // Turning SMS reading on first requests the RECEIVE_SMS permission; we only persist
    // the "enabled" flag when the user actually granted it.
    var requestSmsPermission by remember { mutableStateOf(false) }
    SmsPermissionLauncher(
        trigger = requestSmsPermission,
        onResult = { granted ->
            requestSmsPermission = false
            viewModel.onIntent(ManageToolsIntent.SetSmsReading(granted))
        }
    )

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
                WidgetCard(title = stringResource(Res.string.manage_capabilities_title)) {
                    SettingItem(
                        title = stringResource(Res.string.capability_sms_title),
                        icon = Icons.Default.Sms,
                        on = state.smsReadingEnabled,
                        onToggle = { enabled ->
                            if (enabled) {
                                requestSmsPermission = true
                            } else {
                                viewModel.onIntent(ManageToolsIntent.SetSmsReading(false))
                            }
                        }
                    )
                }
            }

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
