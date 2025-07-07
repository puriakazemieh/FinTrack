package com.kazemieh.transaction.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.model.TransactionWithRelations
import com.kazemieh.transaction.R
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TransactionEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (state.isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(state.transactions) { transactionWithRelations ->
                TransactionItem(
                    transaction = transactionWithRelations,
                    onDelete = { viewModel.onEvent(TransactionEvent.DeleteTransaction(transactionWithRelations.transaction)) }
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionWithRelations,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${stringResource(id = R.string.amount)}: ${transaction.transaction.amount}")
            Text("${stringResource(id = R.string.category)}: ${transaction.category.name}")
            Text("${stringResource(id = R.string.financial_source)}: ${transaction.financialSource.name}")
            Text("${stringResource(id = R.string.tags)}: ${transaction.tags.joinToString { it.name }}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDelete) {
                Text(stringResource(id = R.string.delete))
            }
        }
    }
}
