package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.transaction.ui.component.transactionListContent
import org.koin.androidx.compose.koinViewModel


@Composable
fun rememberTransactionItemsProvider(
    listState: LazyListState,
    viewModel: TransactionViewModel = koinViewModel(),
    onEdit: (TransactionWithRelations) -> Unit = {},
    onDelete: (TransactionWithRelations) -> Unit = {},
): LazyListScope.() -> Unit {

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(TransactionIntent.Init)
    }

    LaunchedEffect(
        listState,
        state.items.size,
        state.endReached,
        state.isAppending,
        state.isRefreshing
    ) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible
        }.collect { lastVisibleIndex ->
            val shouldLoadMore = lastVisibleIndex >= (state.items.size - 5).coerceAtLeast(0)
            if (shouldLoadMore && !state.endReached && !state.isAppending && !state.isRefreshing) {
                viewModel.onIntent(TransactionIntent.LoadNextPage)
            }
        }
    }

    val onEditState = rememberUpdatedState(onEdit)
    val onDeleteState = rememberUpdatedState(onDelete)

    return remember(state) {
        {
            transactionListContent(
                state = state,
                onEdit = { onEditState.value(it) },
                onDelete = { onDeleteState.value(it) },
                onRetryRefresh = { viewModel.onIntent(TransactionIntent.Refresh) },
                onRetryAppend = { viewModel.onIntent(TransactionIntent.LoadNextPage) },
            )
        }
    }
}