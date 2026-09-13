package com.kazemieh.financialsource.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SourceDetailScreen(
    sourceId: Long,
    onBack: () -> Unit,
    viewModel: SourceDetailViewModel = koinViewModel { parametersOf(sourceId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.recent_transactions)
    )

    FintrackScreen(
        title = state.source?.name,
        sub = stringResource(Res.string.title_source_management),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(space.medium))

            if (selectedTabIndex == 0) {
                EntityList(
                    title = stringResource(Res.string.recent_transactions),
                    query = "",
                    onQueryChange = {},
                    items = state.transactions.map { twr ->
                        EntityItem(
                            id = twr.transaction.id,
                            name = twr.transaction.description ?: stringResource(Res.string.transaction),
                            sub = twr.transaction.amount.toSignedPersianPrice(),
                            iconId = twr.category?.iconId ?: 1,
                            colorId = twr.category?.colorId ?: 1
                        )
                    },
                    onItemClick = { },
                    onEditClick = { },
                    onDeleteClick = { },
                    onAddClick = { },
                    showActions = false,
                    showAddFab = false
                )
            }
        }
    }
}
