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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.PieChart
import com.kazemieh.designsystem.component.PieChartItem
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun TotalTransactionCard(
    viewModel: TransactionViewModel = koinViewModel(),
    enableAnimationChart: Boolean = true,
) {
    val space = LocalSpacing.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val incoming = stringResource(Res.string.incoming)
    val outcoming = stringResource(Res.string.outcoming)
    val transfer = stringResource(Res.string.transfer)

    val piChartData by remember(
        incoming, outcoming, transfer,
        state.totalIncome, state.totalExpense, state.totalTransfer
    ) {
        derivedStateOf {
            buildList {
                if (state.totalIncome != 0L)
                    add(PieChartItem(label = incoming, value = state.totalIncome))
                if (state.totalExpense != 0L)
                    add(PieChartItem(label = outcoming, value = state.totalExpense))
                if (state.totalTransfer != 0L)
                    add(PieChartItem(label = transfer, value = state.totalTransfer))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = space.mediumSmall),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = space.one)
    ) {
        Column(
            modifier = Modifier.padding(space.large),
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                FintrackHeadlineSmallText(text = stringResource(Res.string.balance_total))

                val balanceTotalLabel = stringResource(Res.string.balance_total_label, state.balance)

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
                        .padding(vertical = space.mediumSmall)
                        .height(space.one)
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )


                Box(modifier = Modifier.padding(top = space.large)) {
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