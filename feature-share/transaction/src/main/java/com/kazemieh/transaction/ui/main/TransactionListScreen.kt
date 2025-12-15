package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.transaction.ui.component.transactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

    val uiTransactionWithRelations = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()


    TransactionListContent(uiTransactionWithRelations, state.isLoading) {
        viewModel.onIntent(TransactionIntent.DeleteTransaction(it.transaction))
    }

}

@Composable
private fun TransactionListContent(
    uiTransactionWithRelations: LazyPagingItems<TransactionWithRelations>,
    loading: Boolean,
    onDelete: (TransactionWithRelations) -> Unit = {}
) {
    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.height(300.dp)
        ) {
            transactionListContent(uiTransactionWithRelations, onDelete)
        }
    }
}


