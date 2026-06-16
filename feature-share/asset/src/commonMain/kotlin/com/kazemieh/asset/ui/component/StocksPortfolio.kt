package com.kazemieh.asset.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun StocksPortfolio(
    assets: List<Asset>
) {
    val stocks = assets.filter { it.type == AssetType.STOCK }
    
    if (stocks.isEmpty()) return

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        FintrackTitleMediumText(stringResource(Res.string.title_stocks_portfolio))
        Spacer(modifier = Modifier.height(8.dp))
        
        stocks.forEach { stock ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FintrackBodyMediumText(stock.name)
                    FintrackBodyMediumText(stringResource(Res.string.label_shares_suffix, stock.quantity))
                }
            }
        }
    }
}
