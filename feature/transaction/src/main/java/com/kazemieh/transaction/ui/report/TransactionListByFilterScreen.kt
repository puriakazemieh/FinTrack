package com.kazemieh.transaction.ui.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.transaction.ui.component.TransactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListByFilterScreen(
    transactionType: Int? = null,
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(transactionType) {
        viewModel.onIntent(TransactionReportIntent.LoadTransactionsByFilter)
    }


    TransactionListContent(state.isLoading, state.uiTransactionWithRelations) {
        viewModel.onIntent(TransactionReportIntent.DeleteTransactionReport(it.transaction))
    }
}


