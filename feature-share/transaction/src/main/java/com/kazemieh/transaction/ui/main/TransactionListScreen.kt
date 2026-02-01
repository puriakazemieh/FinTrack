package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.transaction.ui.component.transactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListScreen(
    screenHeight: Dp,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

    val uiTransactionWithRelations = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()


    TransactionListContent(uiTransactionWithRelations, state.isLoading, screenHeight) {
        viewModel.onIntent(TransactionIntent.OnDeleteClicked(it.transaction))
    }

}

@Composable
private fun TransactionListContent(
    uiTransactionWithRelations: LazyPagingItems<TransactionWithRelations>,
    loading: Boolean,
    screenHeight: Dp,
    onDelete: (TransactionWithRelations) -> Unit = {}
) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = Modifier.height(screenHeight)
    ) {
        transactionListContent(uiTransactionWithRelations, loading, onDelete)
    }

}

@Composable
fun TransactionItemsProvider(
    viewModel: TransactionViewModel = koinViewModel(),
    onEdit: (TransactionWithRelations) -> Unit = {},
    onDelete: (TransactionWithRelations) -> Unit = {},
    onItemsReady: (LazyListScope.() -> Unit) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

    val uiTransactionWithRelations = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()

    onItemsReady {
        transactionListContent(
            uiTransactionWithRelations,
            state.isLoading,
            onDelete = onDelete,
            onEdit = onEdit
        )
    }
}
