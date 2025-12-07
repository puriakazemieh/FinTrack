package com.kazemieh.transaction.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.paging.compose.collectAsLazyPagingItems
import com.kazemieh.transaction.ui.component.TransactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

//    val uiTransactionWithRelations = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()
//
//
//    TransactionListContent(uiTransactionWithRelations, state.isLoading) {
//        viewModel.onIntent(TransactionIntent.DeleteTransaction(it.transaction))
//    }
}


