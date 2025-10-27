package com.kazemieh.transaction.ui.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.kazemieh.transaction.ui.component.TransactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is TransactionEffect.ShowMessage) {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    TransactionListContent(state.isLoading, state.uiTransactionWithRelations) {
        viewModel.onIntent(TransactionIntent.DeleteTransaction(it.transaction))
    }
}


