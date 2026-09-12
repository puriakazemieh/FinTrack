package com.kazemieh.tag.ui.detail

import androidx.compose.foundation.layout.*
import com.kazemieh.common.toSignedPersianPrice
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntityItem
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TagDetailScreen(
    tagId: Long,
    onBack: () -> Unit,
    onNavigateToNoteEdit: (Long) -> Unit,
    viewModel: TagDetailViewModel = koinViewModel { parametersOf(tagId) }
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.recent_transactions),
        stringResource(Res.string.notes)
    )

    FintrackScreen(
        title = state.tag?.name ?: "",
        sub = stringResource(Res.string.title_tag_management),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                // Transactions
                // We'll reuse the UI pattern. For simplicity, map to EntityItems
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
                    onItemClick = { }, onEditClick = { }, onDeleteClick = { },
                    onAddClick = { },
                    showActions = false
                )
            } else {
                // Notes
                EntityList(
                    title = stringResource(Res.string.notes),
                    query = "",
                    onQueryChange = {},
                    items = state.notes.map { note ->
                        EntityItem(
                            id = note.id,
                            name = note.title,
                            sub = note.content.take(30),
                            iconId = 1,
                            colorId = 1
                        )
                    },
                    onItemClick = { item -> onNavigateToNoteEdit(item.id) }, onEditClick = { }, onDeleteClick = { },
                    onAddClick = { },
                    showActions = false
                )
            }
        }
    }
}


