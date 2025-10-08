package com.kazemieh.transaction.ui

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackHeadlineMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.model.TransactionWithRelations
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn {
                item {
                    FintrackTitleMediumText(
                        text = stringResource(R.string.recent_transactions),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(state.transactions) { transactionWithRelations ->
                    TransactionItem(
                        transaction = transactionWithRelations,
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


@Composable
fun TransactionItem(
    transaction: TransactionWithRelations,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            FintrackTitleMediumText(
                text = "${transaction.category.name} - ${transaction.financialSource.name}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            val amountColor =
                if (transaction.transaction.amount >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error

            FintrackBodyLargeText(
                text = stringResource(
                    R.string.amount_label,
                    transaction.transaction.amount.toString()
                ),
                color = amountColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            val tagsText = transaction.tags.joinToString { it.name }
            FintrackBodySmallText(
                text = stringResource(R.string.tags_label, tagsText),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                FintrackBodyMediumText(text = stringResource(R.string.delete))
            }
        }
    }
}


@Composable
fun ShowTransactionCard(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FintrackTitleMediumText(text = stringResource(R.string.balance_total))

            FintrackHeadlineMediumText(
                text = state.balance,
                color = if (state.isPositiveBalance) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FintrackBodyMediumText(
                        text = stringResource(R.string.outcoming),
                        color = MaterialTheme.colorScheme.error
                    )
                    FintrackTitleMediumText(
                        text = state.totalExpense,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FintrackBodyMediumText(
                        text = stringResource(R.string.incoming),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    FintrackTitleMediumText(
                        text = state.totalIncome,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
