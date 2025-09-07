package com.kazemieh.financialsource.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddFinancialSourceScreen(
    viewModel: AddFinancialSourceViewModel = koinViewModel(),
    onFinancialSourceAdded: () -> Unit
) {
    val name = remember { mutableStateOf("") }
    val balance = remember { mutableDoubleStateOf(0.0) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("نام منبع") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = if (balance.doubleValue == 0.0) "" else balance.doubleValue.toString(),
            onValueChange = { input ->
                val newValue = input.toDoubleOrNull()
                if (newValue != null) {
                    balance.doubleValue = newValue
                } else if (input.isEmpty()) {
                    balance.doubleValue = 0.0
                }
            },
            label = { Text("مقدار اولیه") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.addFinancialSource(name.value, balance.doubleValue)
                onFinancialSourceAdded()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFinancialSourceBottomSheet(
    viewModel: AddFinancialSourceViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    onFinancialSourceAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val balance = remember { mutableDoubleStateOf(0.0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("افزودن منبع جدید", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام منبع") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = if (balance.doubleValue == 0.0) "" else balance.doubleValue.toString(),
                onValueChange = { input ->
                    val newValue = input.toDoubleOrNull()
                    if (newValue != null) {
                        balance.doubleValue = newValue
                    } else if (input.isEmpty()) {
                        balance.doubleValue = 0.0
                    }
                },
                label = { Text("مقدار اولیه") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addFinancialSource(name, balance.doubleValue)
                        onFinancialSourceAdded()
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره")
            }
        }
    }
}