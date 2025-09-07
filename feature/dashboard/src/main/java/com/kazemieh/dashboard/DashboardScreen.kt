package com.kazemieh.dashboard


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateAddSource: () -> Unit,
    onNavigateAddTransaction: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.LoadData)
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ✅ بخش موجودی کل
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("موجودی کل: ${state.totalBalance}")
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Text("پرداخت: ${state.totalExpense}", Modifier.weight(1f))
                            Text("دریافت: ${state.totalIncome}", Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ✅ بخش منابع مالی
            item {
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
            }
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items(state.financialSources) { source ->
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(source.name)
                                Text("موجودی: ${source.balance}")
                            }
                        }
                    }
                }
            }


            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "تراکنش‌های اخیر",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onNavigateAddTransaction) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن تراکنش")
                    }
                }
            }
            items(state.recentTransactions) { trx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("مبلغ: ${trx.transaction.amount}")
                        Text("دسته: ${trx.category.name}")
                        Text("منبع: ${trx.financialSource.name}")
                    }
                }
            }
        }
    }
}
