package com.kazemieh.transaction.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.transaction.ui.component.TransactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
//    val context = LocalContext.current
//    val snackbarHostState = remember { SnackbarHostState() }
//    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
//            if (effect is TransactionEffect.ShowMessage) {
//                coroutineScope.launch {
//                    snackbarHostState.showSnackbar(
//                        message = context.getString(effect.message),
//                        duration = SnackbarDuration.Short
//                    )
//                }
//            }
        }
    }

    TransactionListContent(state.uiTransactionWithRelations, state.isLoading) {
        viewModel.onIntent(TransactionIntent.DeleteTransaction(it.transaction))
    }
}


