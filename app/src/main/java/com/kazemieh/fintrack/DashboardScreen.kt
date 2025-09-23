package com.kazemieh.fintrack


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.financialsource.ui.SourceList
import com.kazemieh.financialsource.ui.SourceListBottomSheet
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.transaction.ui.ShowTransactionCard
import com.kazemieh.transaction.ui.TransactionList
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(DashboardIntent.ShowAddTransaction(true)) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن تراکنش"
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (state.showAddTransaction) {
                AddTransactionBottomSheet(
                    setSource = state.setSource,
                    onDismiss = {
                        viewModel.onIntent(DashboardIntent.ShowAddTransaction(false))
                    },
                    onSourceClicked = {
                        viewModel.onIntent(DashboardIntent.SourceList(true))
                    }
                )
            }

            if (state.sourceList) {
                SourceListBottomSheet(
                    onAddSourceClick = {
                        viewModel.onIntent(
                            DashboardIntent.ShowAddSource(
                                showAddSource = true,
                                fromSourceList = true
                            )
                        )
                    },
                    onSourceClick = { id, name ->
                        viewModel.onIntent(DashboardIntent.SetSource(id to name))
                    },
                    onDismiss = {
                        viewModel.onIntent(DashboardIntent.SourceList(false))
                    }
                )
            }

            if (state.showAddSource) {
                AddSourceBottomSheet(
                    onDismiss = {
                        viewModel.onIntent(
                            DashboardIntent.ShowAddSource(
                                showAddSource = false,
                                fromSourceList = false
                            )
                        )
                    },
                    setSource = { id, name ->
                        if (state.fromSourceList)
                            viewModel.onIntent(DashboardIntent.SetSource(id to name))
                        else viewModel.onIntent(
                            DashboardIntent.ShowAddSource(
                                showAddSource = false,
                                fromSourceList = false
                            )
                        )
                    }
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {


                ShowTransactionCard()

                Spacer(Modifier.height(16.dp))


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "منابع مالی",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.onIntent(
                            DashboardIntent.ShowAddSource(
                                showAddSource = true,
                                fromSourceList = false
                            )
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن منبع")
                    }
                }

                SourceList()

                Spacer(Modifier.height(16.dp))

                TransactionList()

            }
        }
    }
}
