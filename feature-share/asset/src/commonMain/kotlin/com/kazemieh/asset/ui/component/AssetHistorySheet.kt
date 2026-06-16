package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
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
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                FintrackTitleLargeText(stringResource(Res.string.title_price_history, asset.name))
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
                            FintrackBodyMediumText(stringResource(Res.string.mock_date, 10 + i))
                            FintrackBodyMediumText(asset.purchasePrice.toSignedPersianPrice())
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
