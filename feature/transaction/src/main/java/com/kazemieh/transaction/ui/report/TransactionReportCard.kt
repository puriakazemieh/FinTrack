package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.PieChart
import com.kazemieh.transaction.R
import org.koin.androidx.compose.koinViewModel


@Composable
fun ShowTransactionReportCard(
    viewModel: TransactionReportViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(TransactionReportIntent.LoadTransactionsByFilter)
    }

    val text = when (state.selectedTransactionType.count) {
        1 -> {
            stringResource(R.string.incoming)
        }

        2 -> {
            stringResource(R.string.outcoming)
        }

        else -> {
            stringResource(R.string.all)
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


            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                FintrackTitleSmallText(text = stringResource(R.string.balance_with_label, text))

                val balanceTotalLabel = stringResource(R.string.balance_total_label, state.balance)

                FintrackTitleSmallText(
                    modifier = Modifier.weight(1f),
                    text = balanceTotalLabel,
                    textAlign = TextAlign.End,
                    color = if (state.isPositiveBalance) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )

            }

            Spacer(Modifier.height(8.dp))

            if (state.pieChartData.isNotEmpty()) {
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
                        data = state.pieChartData,
                        radiusOuter = 40.dp,
                        chartBarWidth = 20.dp,
                        textDistanceExtra = 20.dp,
                        animDuration = 500,
                    )

                }
            }
        }
    }
}