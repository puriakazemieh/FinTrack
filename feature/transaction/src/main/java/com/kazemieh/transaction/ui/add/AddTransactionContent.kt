package com.kazemieh.transaction.ui.add

import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row {
            RadioButton(
                selected = state.isIncome,
                onClick = { onEvent(AddTransactionEvent.SetIsIncome(true)) }
            )
            Text(text = "درآمد")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = !state.isIncome,
                onClick = { onEvent(AddTransactionEvent.SetIsIncome(false)) }
            )
            Text(text = "هزینه")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.amount,
            onValueChange = { onEvent(AddTransactionEvent.SetAmount(it)) },
            label = { Text("مبلغ") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(AddTransactionEvent.SetDescription(it)) },
            label = { Text("توضیحات") },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "دسته‌بندی",
            options = state.categories,
            selectedOption = state.selectedCategory,
            optionToString = { it.name },
            onOptionSelected = { onEvent(AddTransactionEvent.SetCategory(it)) }
        )

        DropdownSelector(
            label = "منبع مالی",
            options = state.financialSources,
            selectedOption = state.selectedFinancialSource,
            optionToString = { it.name },
            onOptionSelected = { onEvent(AddTransactionEvent.SetFinancialSource(it)) }
        )

        TagSelector(
            tags = state.tags,
            selectedTags = state.selectedTags,
            onTagClick = { onEvent(AddTransactionEvent.ToggleTag(it)) }
        )

        DatePickerField(
            selectedDate = state.selectedDate ,
            onDateSelected = { onEvent(AddTransactionEvent.SetDate(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(AddTransactionEvent.Submit) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره تراکنش")
        }
    }
}
