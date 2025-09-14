package com.kazemieh.dashboard


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateAddSource: () -> Unit,
    onNavigateAddTransaction: () -> Unit,
    onShowAllTransaction: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.LoadData)
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateAddTransaction
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن تراکنش"
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // ✅ بخش موجودی کل
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(" موجودی کل : ", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = " ${state.totalBalance}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (state.totalBalance >= 0) Color(0xFF2E7D32) else Color.Red
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    " پرداخت : ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32)
                                )
                                val plus = if (state.totalExpense > 0) "+" else ""
                                Text(
                                    text = "$plus${state.totalExpense}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    " دریافت : ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Red
                                )
                                val mines = if (state.totalIncome < 0) "-" else ""
                                Text(
                                    text = "$mines${state.totalIncome}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))


                // ✅ بخش منابع مالی
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "منابع مالی",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onNavigateAddSource) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن منبع")
                    }
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items(state.financialSources) { source ->
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Text(source.name)
                                Text("${source.balance} T")
                            }
                        }
                    }
                }


                Spacer(Modifier.height(16.dp))

                onShowAllTransaction()

            }
        }
    }
}
