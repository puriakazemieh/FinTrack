package com.kazemieh.transaction.ui

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackDisplayLargeText
import com.kazemieh.designsystem.component.FintrackDisplayMediumText
import com.kazemieh.designsystem.component.FintrackHeadlineLargeText
import com.kazemieh.designsystem.component.FintrackHeadlineMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.PieChart
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.model.TransactionType
import com.kazemieh.transaction.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionList(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is TransactionEffect.ShowMessage) {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column {
                FintrackTitleMediumText(
                    text = stringResource(R.string.recent_transactions),
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.uiTransactionWithRelations) { transactionWithRelations ->
                        TransactionItem(
                            uiTransactionWithRelation = transactionWithRelations,
                            onDelete = {
                                viewModel.onEvent(
                                    TransactionEvent.DeleteTransaction(transactionWithRelations.transaction)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TransactionItem(
    uiTransactionWithRelation: TransactionWithRelationsUi,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackTitleMediumText(
                    text = uiTransactionWithRelation.categoryName,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            FintrackTitleMediumText(
                text = "${stringResource(R.string.source)} : ${uiTransactionWithRelation.financialSourceName}",
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            val amountColor =
                if (uiTransactionWithRelation.transaction.amount >= 0)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.error

            val type = if (uiTransactionWithRelation.transaction.type == TransactionType.INCOME)
                stringResource(R.string.incoming)
            else stringResource(R.string.outcoming)

            FintrackBodyLargeText(
                text = stringResource(
                    R.string.amount_label,
                    type,
                    uiTransactionWithRelation.transaction.formatedAmount
                ),
                color = amountColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (uiTransactionWithRelation.tags.isNotEmpty()) {
                FintrackBodySmallText(
                    text = stringResource(R.string.tags_label, uiTransactionWithRelation.tags),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


    }
}


@Composable
fun ShowTransactionCard(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val incoming = stringResource(R.string.incoming)
    val incomingItem = PieChartItem(label = incoming, value = state.totalIncome)

    val outcoming = stringResource(R.string.outcoming)
    val outcomingItem = PieChartItem(label = outcoming, value = state.totalExpense)


    val sampleData = listOf(incomingItem, outcomingItem)

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

                FintrackHeadlineMediumText(text = stringResource(R.string.balance_total))

                val balanceTotalLabel = stringResource(R.string.balance_total_label, state.balance)

                FintrackHeadlineMediumText(
                    modifier = Modifier.weight(1f),
                    text = balanceTotalLabel,
                    textAlign = TextAlign.End,
                    color = if (state.isPositiveBalance) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )

            }

            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )

            PieChart(
                data = sampleData,
                radiusOuter = 50.dp,
                chartBarWidth = 20.dp,
                textDistanceExtra = 20.dp,
                animDuration = 500,
            )


        }
    }
}
