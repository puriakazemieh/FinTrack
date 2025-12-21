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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.PieChart
import org.koin.androidx.compose.koinViewModel


@Composable
fun ShowTransactionReportCard(
    viewModel: TransactionReportViewModel = koinViewModel(),
    enableAnimationChart: Boolean = true,
) {

    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current


    val text = when (state.filterParams.type) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = space.one)
    ) {

        Column(
            modifier = Modifier.padding(space.large),
        ) {


            Spacer(Modifier.height(space.mediumSmall))

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

            Spacer(Modifier.height(space.mediumSmall))

            if (state.pieChartData.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(vertical = space.mediumSmall)
                        .height(space.one)
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )


                Box(modifier = Modifier.padding(top =space.large)) {
                    PieChart(
                        data = state.pieChartData,
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