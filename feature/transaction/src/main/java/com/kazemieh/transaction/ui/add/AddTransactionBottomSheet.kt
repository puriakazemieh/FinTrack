package com.kazemieh.transaction.ui.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: AddTransactionViewModel = koinViewModel(),
    source: Pair<Int, String>? = null,
    category: Pair<Int, String>? = null,
    tags: Set<Pair<Int, String>>? = null,
    onDismiss: () -> Unit,
    onSourceClicked: () -> Unit,
    onCategoryClicked: () -> Unit,
    onTagClicked: () -> Unit,
    transactionAdded: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    viewModel.onEvent(AddTransactionEvent.SetSource(source))
    viewModel.onEvent(AddTransactionEvent.SetCategory(category))
    viewModel.onEvent(AddTransactionEvent.SetTags(tags))

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTransactionEffect.Success -> {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            transactionAdded()
                        }
                    }
                }

                is AddTransactionEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }

                AddTransactionEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onEvent(AddTransactionEvent.OnDismiss) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AddTransactionContent(
                state = state,
                onEvent = viewModel::onEvent,
                onSourceClicked = onSourceClicked,
                onTagClicked = onTagClicked,
                onCategoryClicked = onCategoryClicked
            )
        }
    }
}


@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onSourceClicked: () -> Unit,
    onCategoryClicked: () -> Unit,
    onTagClicked: () -> Unit,
    onEvent: (AddTransactionEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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

        Selector(
            label = "دسته بندی را انتخاب کنید",
            item = state.category?.second ?: "انتخاب کنید",
            onClicked = onCategoryClicked,
        )
        Selector(
            label = "منبع مالی را انتخاب کنید",
            item = state.source?.second ?: "انتخاب کنید",
            onClicked = onSourceClicked,
        )
//        Text(
//            text = state.category?.second ?: "انتخاب کنید",
//            modifier = Modifier.clickable { onCategoryClicked() })
//
//        Text(
//            text = state.source?.second ?: "انتخاب کنید",
//            modifier = Modifier.clickable { onSourceClicked() })

        if (state.tags != null) {

            Column(Modifier.fillMaxWidth()) {
                Selector(
                    label = "تگ ها را انتخاب کنید",
                    item = "تغییر دهید",
                    onClicked = onTagClicked,
                )
                Spacer(Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.tags.forEach { tag ->
                        TextButton(
                            onClick = {

                            }
                        ) {
                            Text(tag.second)
                        }
                    }
                }

            }
        } else {
//            Text(
//                text = "انتخاب کنید",
//                modifier = Modifier.clickable { onTagClicked() })

            Selector(
                label = "تگ ها را انتخاب کنید",
                item = "لیست تگ ها",
                onClicked = onTagClicked,
            )
        }


        DatePickerField(
            selectedDate = state.selectedDate,
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
