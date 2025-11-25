package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.PieChart
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.transaction.R
import org.koin.androidx.compose.koinViewModel


@Composable
fun TotalTransactionCard(
    viewModel: TransactionViewModel = koinViewModel(),
    enableAnimationChart: Boolean = true,
) {
    LaunchedEffect(true) {
        viewModel.onIntent(TransactionIntent.LoadTransactions)
    }

    val state by viewModel.state.collectAsState()

    val incoming = stringResource(R.string.incoming)
    val outcoming = stringResource(R.string.outcoming)
    val transfer = stringResource(R.string.transfer)

    val piChartData by remember(state.totalIncome, state.totalExpense, state.totalTransfer) {
        derivedStateOf {
            buildList {
                if (state.totalIncome != 0L)
                    add(PieChartItem(label = incoming, value = state.totalIncome))
                if (state.totalExpense != 0L)
                    add(PieChartItem(label = outcoming, value = state.totalExpense))
                if (state.totalExpense != 0L)
                    add(PieChartItem(label = transfer, value = state.totalExpense))
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                FintrackHeadlineSmallText(text = stringResource(R.string.balance_total))

                val balanceTotalLabel = stringResource(R.string.balance_total_label, state.balance)

                FintrackHeadlineSmallText(
                    modifier = Modifier.weight(1f),
                    text = balanceTotalLabel,
                    textAlign = TextAlign.End,
                    color = if (state.isPositiveBalance) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )

            }

            if (piChartData.any { it.value.toInt() > 0 }) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .height(1.dp)
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )


                Box(modifier = Modifier.padding(top = 20.dp)) {
                    PieChart(
                        data = piChartData,
                        radiusOuter = 40.dp,
                        chartBarWidth = 20.dp,
                        textDistanceExtra = 20.dp,
                        animDuration = 500,
                        enableAnimation = enableAnimationChart
                    )

                }
            }
        }
    }
}