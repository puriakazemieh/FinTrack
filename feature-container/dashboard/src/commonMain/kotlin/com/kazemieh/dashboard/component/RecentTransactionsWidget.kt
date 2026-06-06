package com.kazemieh.dashboard.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.SwipeableTxRow
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.transaction.ui.main.TransactionIntent
import com.kazemieh.transaction.ui.main.TransactionViewModel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.recent_transactions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecentTransactionsWidget(
    viewModel: TransactionViewModel = koinViewModel(),
    onMoreClick: () -> Unit,
    onEdit: (TransactionWithRelations) -> Unit = {},
    onDelete: (TransactionWithRelations) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(TransactionIntent.Init)
    }

    WidgetCard(
        title = stringResource(Res.string.recent_transactions),
        onMore = onMoreClick,
        modifier = modifier
    ) {
        state.items.take(5).forEach { item ->
            SwipeableTxRow(
                item = item,
                onClick = { onEdit(item) },
                onDelete = { onDelete(item) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
