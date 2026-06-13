package com.kazemieh.asset.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.designsystem.component.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    onBack: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.GOLD) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { FintrackTitleLargeText("افزودن دارایی") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FintrackOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { FintrackBodyMediumText("نام دارایی (مثلاً دلار یا طلای ۱۸)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Asset Type Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AssetType.entries.forEach { assetType ->
                    FilterChip(
                        selected = type == assetType,
                        onClick = { type = assetType },
                        label = { FintrackLabelMediumText(assetType.name) }
                    )
                }
            }

            FintrackOutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { FintrackBodyMediumText("مقدار / تعداد") },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = { FintrackBodyMediumText("قیمت خرید هر واحد") },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { FintrackBodyMediumText("توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            FintrackButton(
                text = "ذخیره",
                onClick = {
                    val asset = Asset(
                        name = name,
                        type = type,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toLongOrNull() ?: 0L,
                        description = description,
                        colorId = 1,
                        iconId = 1
                    )
                    viewModel.onIntent(AssetIntent.AddAsset(asset))
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
