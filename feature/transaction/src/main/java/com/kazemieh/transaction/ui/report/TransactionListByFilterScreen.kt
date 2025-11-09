package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.transaction.ui.component.TransactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListByFilterScreen(
    selectedSources: Set<Pair<Int, String>>,
    selectedCategories: Set<Pair<Int, String>>,
    selectedTransactionType: Int,
    fromTimestamp: Long? = null,
    toTimestamp: Long? = null,
    enableAnimationChart: Boolean = true,
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(selectedCategories) {
        viewModel.onIntent(TransactionReportIntent.SelectedCategory(selectedCategories))
    }

    LaunchedEffect(selectedSources) {
        viewModel.onIntent(TransactionReportIntent.SelectedSource(selectedSources))
    }

    LaunchedEffect(selectedTransactionType) {
        viewModel.onIntent(TransactionReportIntent.SelectedType(selectedTransactionType))
    }

    LaunchedEffect(fromTimestamp, toTimestamp) {
        viewModel.onIntent(TransactionReportIntent.SelectedDate(fromTimestamp, toTimestamp))
    }

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionReportIntent.LoadTransactionsByFilter)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            ShowTransactionReportCard(enableAnimationChart = enableAnimationChart)
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            TransactionListContent(state.uiTransactionWithRelations, state.isLoading) {
                viewModel.onIntent(TransactionReportIntent.DeleteTransactionReport(it.transaction))
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }


}


