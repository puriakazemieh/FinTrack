package com.kazemieh.financialsource.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddFinancialSourceScreen(
    viewModel: AddFinancialSourceViewModel = koinViewModel(),
    onFinancialSourceAdded: () -> Unit
) {
    val name = remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("نام منبع") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.addFinancialSource(name.value)
                onFinancialSourceAdded()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره")
        }
    }
}
