package com.kazemieh.check.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kazemieh.check.ui.add.AddCheckBottomSheet
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.Tabs
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckListScreen(
    onBack: () -> Unit,
    viewModel: CheckListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddCheck by remember { mutableStateOf(false) }

    val tabs = listOf(
        stringResource(Res.string.label_check_status_ongoing),
        stringResource(Res.string.label_check_status_passed),
        stringResource(Res.string.label_check_status_returned),
        stringResource(Res.string.label_check_status_cancelled),
    )

    val currentStatus = when (selectedTab) {
        0 -> CheckStatus.PENDING
        1 -> CheckStatus.PASSED
        2 -> CheckStatus.REJECTED
        else -> CheckStatus.CANCELLED
    }

    val filteredChecks = state.checks.filter { it.status == currentStatus }
    val totalAmount = filteredChecks.sumOf { it.amount }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
                .padding(padding)
                .padding(100.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScreenHeader(
                    title = stringResource(Res.string.title_check_management),
                    sub = stringResource(Res.string.sub_check_list_management_desc),
                    onClose = onBack
                )

                Tabs(
                    tabs = tabs,
                    active = selectedTab,
                    onChange = { selectedTab = it },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    counts = listOf(
                        state.checks.count { it.status == CheckStatus.PENDING },
                        state.checks.count { it.status == CheckStatus.PASSED },
                        state.checks.count { it.status == CheckStatus.REJECTED },
                        state.checks.count { it.status == CheckStatus.CANCELLED }
                    )
                )

                EntityList(
                    title = stringResource(Res.string.title_check_management),
                    query = "",
                    onQueryChange = {},
                    onAddClick = { showAddCheck = true },
                    summary = listOf(
                        EntitySummary(
                            label =  stringResource(Res.string.label_total_amount),
                            value = totalAmount.toPersianPrice(),
                            unit = stringResource(Res.string.currency_toman),
                            color = MaterialTheme.colorScheme.primary
                        )
                    ),
                    items = filteredChecks.map {
                        EntityItem(
                            id = it.id,
                            name = it.personName ?: stringResource(Res.string.label_unknown_person),
                            sub = it.description,
                            badge = it.amount.toPersianPrice(),
                            color = if (it.isIncoming) GlassGreen else GlassRed
                        )
                    },
                    onEditClick = { /* TODO */ },
                    onDeleteClick = { viewModel.onIntent(CheckListIntent.DeleteCheck(it.id)) },
                    showActions = true
                )
            }
        }
    }

    if (showAddCheck) {
        AddCheckBottomSheet(
            onDismiss = { showAddCheck = false }
        )
    }
}
