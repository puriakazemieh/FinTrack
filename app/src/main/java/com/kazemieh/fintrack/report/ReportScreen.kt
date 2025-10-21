package com.kazemieh.fintrack.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.transaction.ui.ShowTransactionReportCard
import com.kazemieh.transaction.ui.TransactionList
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                ShowTransactionReportCard { viewModel.onIntent(ReportIntent.SelectedType(it)) }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item { TransactionList(transactionType = state.transactionType) }
        }
    }
}