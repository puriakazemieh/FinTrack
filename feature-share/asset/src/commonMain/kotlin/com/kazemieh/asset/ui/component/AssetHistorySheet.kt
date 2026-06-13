package com.kazemieh.asset.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.component.FintrackText
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetHistorySheet(
    asset: Asset,
    onDismiss: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    // In a real app, we'd fetch history for this specific asset
    // For now, it's a placeholder for history view
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            FintrackText("تاریخچه قیمت: ${asset.name}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(300.dp)
            ) {
                // Mock history data
                items(5) { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FintrackText("۱۴۰۲/۰۳/${10+i}")
                        FintrackText(asset.purchasePrice.toSignedPersianPrice())
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
