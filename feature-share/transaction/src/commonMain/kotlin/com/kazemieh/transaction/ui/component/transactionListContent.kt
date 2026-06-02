package com.kazemieh.transaction.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.kazemieh.designsystem.component.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fintrack.core.designsystem.generated.resources.*
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.transaction.ui.main.TransactionState
import org.jetbrains.compose.resources.stringResource

fun LazyListScope.transactionListContent(
    state: TransactionState,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    onRetryRefresh: () -> Unit = {},
    onRetryAppend: () -> Unit = {},
) {
    val isEmpty = state.items.isEmpty()

    if (state.isRefreshing && isEmpty) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    if (state.refreshError != null && isEmpty) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FintrackBodyMediumText(text = stringResource(Res.string.label_error_with_msg, state.refreshError.orEmpty()))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onRetryRefresh) { FintrackBodyMediumText(stringResource(Res.string.label_retry)) }
                }
            }
        }
        return
    }

    // ===== Empty =====
    if (isEmpty) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyListScreen(stringResource(Res.string.transaction))
            }
        }
        return
    }

    // ===== Items =====
    items(
        items = state.items,
        key = { it.transaction.id }
    ) { item ->
        TransactionItem(
            uiTransactionWithRelation = item,
            onDelete = { onDelete(item) },
            onEdit = { onEdit(item) }
        )
    }

    // ===== Append Loading =====
    if (state.isAppending) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // ===== Append Error =====
    if (state.appendError != null) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FintrackBodyMediumText(text = stringResource(Res.string.label_error_with_msg, state.appendError.orEmpty()))
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRetryAppend) { FintrackBodyMediumText(stringResource(Res.string.label_retry)) }
            }
        }
    }
}
