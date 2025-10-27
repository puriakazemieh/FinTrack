package com.kazemieh.fintrack.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FilterButton
import com.kazemieh.financialsource.ui.SourceListSelectionBottomSheet
import com.kazemieh.fintrack.R
import com.kazemieh.transaction.ui.report.ShowTransactionReportCard
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

        Column {

            ReportTopBar(onIntent = viewModel::onIntent, state = state)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    ShowTransactionReportCard()
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
//                    TransactionListByFilterScreen(transactionType = state.transactionType)
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }


        if (state.isSourceSheetVisible) {
            SourceListSelectionBottomSheet(
                onSourceClick = {
                    viewModel.onIntent(ReportFilterIntent.OnSourcesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportFilterIntent.OnToggleSourceSheet)
                }
            )
        }


    }
}


@Composable
fun ReportTopBar(onIntent: (ReportFilterIntent) -> Unit, state: ReportFilterState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterButton(
                    modifier = Modifier.weight(1f),
                    text = if (state.selectedSources.isEmpty()) stringResource(R.string.all_source)
                    else if (state.selectedSources.size == 1) state.selectedSources.first().second
                    else "${stringResource(R.string.sources)} (${state.selectedSources.size})",
                    onClick = { onIntent(ReportFilterIntent.OnToggleSourceSheet) }
                )

                FilterButton(
                    modifier = Modifier.weight(1f),
                    text = if (state.selectedCategories.isEmpty()) stringResource(R.string.all_category)
                    else if (state.selectedCategories.size == 1) state.selectedCategories.first().second
                    else "${stringResource(R.string.categories)} (${state.selectedCategories.size})",
                    onClick = { onIntent(ReportFilterIntent.OnToggleCategorySheet) }
                )
            }


            DatePeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onChange = { onIntent(ReportFilterIntent.OnPeriodChanged(it)) }
            )
        }
    }
}



@Composable
fun DatePeriodSelector(selectedPeriod: ReportPeriod, onChange: (ReportPeriod) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* ماه قبل */ }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ماه قبل")
        }

        Text(
            text = when (selectedPeriod) {
                ReportPeriod.ThisMonth -> "این ماه"
                ReportPeriod.LastMonth -> "ماه قبل"
                ReportPeriod.Custom -> "بازه دلخواه"
            },
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = { /* ماه بعد */ }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه بعد")
        }
    }
}

