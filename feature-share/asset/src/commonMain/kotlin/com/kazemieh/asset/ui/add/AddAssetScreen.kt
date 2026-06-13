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
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackText
import com.kazemieh.designsystem.component.OutlinedTextField
import org.koin.compose.viewmodel.koinViewModel

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
                title = { FintrackText("افزودن دارایی") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { FintrackText("نام دارایی (مثلاً دلار یا طلای ۱۸)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Asset Type Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AssetType.entries.forEach { assetType ->
                    FilterChip(
                        selected = type == assetType,
                        onClick = { type = assetType },
                        label = { FintrackText(assetType.name) }
                    )
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { FintrackText("مقدار / تعداد") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = { FintrackText("قیمت خرید هر واحد") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { FintrackText("توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            FintrackButton(
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
            ) {
                FintrackText("ذخیره")
            }
        }
    }
}
