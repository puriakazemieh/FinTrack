package com.kazemieh.asset.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.component.FintrackText
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssetsListScreen(
    onAddAsset: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { FintrackText("دارایی‌ها") },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(AssetIntent.SyncRates) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAsset) {
                Icon(Icons.Default.Add, contentDescription = "افزودن دارایی")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            TotalValueCard(state.totalValue)
            CompositionBar(state.composition)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.assets) { asset ->
                    AssetRow(asset)
                }
            }
        }
    }
}

@Composable
fun TotalValueCard(totalValue: Long) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FintrackText("ارزش کل دارایی‌ها", style = MaterialTheme.typography.titleMedium)
            FintrackText(totalValue.toSignedPersianPrice(), style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
fun CompositionBar(composition: Map<*, Double>) {
    // Simplified composition bar
    Row(
        modifier = Modifier.fillMaxWidth().height(12.dp).padding(horizontal = 16.dp)
    ) {
        composition.forEach { (type, percentage) ->
            Box(
                modifier = Modifier.weight(percentage.toFloat().coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color((type.hashCode() * 0xFFFFFF) or 0xFF000000.toInt()))
            )
        }
    }
}

@Composable
fun AssetRow(asset: Asset) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FintrackText(asset.name, style = MaterialTheme.typography.titleMedium)
                FintrackText("${asset.quantity} واحد", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                FintrackText(asset.totalCurrentValue.toSignedPersianPrice())
                FintrackText(
                    "${asset.profitOrLossPercentage}%",
                    color = if (asset.profitOrLoss >= 0) Color.Green else Color.Red
                )
            }
        }
    }
}
