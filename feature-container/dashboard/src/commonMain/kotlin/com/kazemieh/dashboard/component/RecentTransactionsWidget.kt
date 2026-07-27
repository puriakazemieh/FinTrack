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
import com.kazemieh.designsystem.component.SwipeableTxRowMinimal
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import com.kazemieh.transaction.ui.main.TransactionIntent
import com.kazemieh.transaction.ui.main.TransactionViewModel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.recent_transactions
import fintrack.core.designsystem.generated.resources.msg_no_transaction_found
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong

@Composable
fun RecentTransactionsWidget(
    viewModel: TransactionViewModel = koinViewModel(),
    onMore: () -> Unit,
    onAdd: () -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    onDelete: (TransactionWithRelations) -> Unit = {},
    onRepeat: (TransactionWithRelations) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(TransactionIntent.Init)
    }

    WidgetCard(
        title = stringResource(Res.string.recent_transactions),
        onMore = onMore,
        onAdd = onAdd,
        modifier = modifier
    ) {
        if (state.items.isEmpty()) {
            WidgetEmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                text = stringResource(Res.string.msg_no_transaction_found)
            )
        } else {
            state.items.take(5).forEach { item ->
                SwipeableTxRowMinimal(
                    item = item,
                    onClick = { onEdit(item) },
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item) },
                    onRepeat = onRepeat
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
