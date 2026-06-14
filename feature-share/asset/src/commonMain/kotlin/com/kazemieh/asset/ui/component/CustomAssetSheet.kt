package com.kazemieh.asset.ui.component

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
fun CustomAssetSheet(
    onDismiss: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FintrackTitleLargeText("افزودن دارایی سفارشی")

            FintrackOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { FintrackBodyMediumText("نام دارایی") },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { FintrackBodyMediumText("مقدار") },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = currentPrice,
                onValueChange = { currentPrice = it },
                label = { FintrackBodyMediumText("ارزش فعلی هر واحد (ریال)") },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { FintrackBodyMediumText("توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackButton(
                text = "ذخیره",
                onClick = {
                    val asset = Asset(
                        name = name,
                        type = AssetType.CUSTOM,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = currentPrice.toLongOrNull() ?: 0L,
                        description = description,
                        colorId = 1,
                        iconId = 1
                    )
                    viewModel.onIntent(AssetIntent.AddAsset(asset))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
