package com.kazemieh.transaction.ui.add

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.component.DatePickerField
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.model.TransactionType
import com.kazemieh.transaction.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: AddTransactionViewModel = koinViewModel(),
    source: Pair<Int, String>? = null,
    category: Pair<Int, String>? = null,
    tags: Set<Pair<Int, String>>? = null,
    persons: Set<Pair<Int, String>>? = null,
    onDismiss: () -> Unit,
    onSourceClicked: () -> Unit,
    onCategoryClicked: (Int) -> Unit,
    onTagClicked: () -> Unit,
    onPersonClicked: () -> Unit,
    transactionAdded: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        viewModel.onEvent(AddTransactionEvent.FetchDefaultData)
    }

    LaunchedEffect(source) {
        viewModel.onEvent(AddTransactionEvent.SetSource(source))
    }
    LaunchedEffect(category) {
        viewModel.onEvent(AddTransactionEvent.SetCategory(category))
    }
    LaunchedEffect(tags) {
        viewModel.onEvent(AddTransactionEvent.SetTags(tags))
    }
    LaunchedEffect(persons) {
        viewModel.onEvent(AddTransactionEvent.SetPerson(persons))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTransactionEffect.Success -> {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            transactionAdded()
                            onDismiss()
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
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        AddTransactionContent(
            state = state,
            onEvent = viewModel::onEvent,
            onSourceClicked = onSourceClicked,
            onTagClicked = onTagClicked,
            onPersonClicked = onPersonClicked,
            onCategoryClicked = { onCategoryClicked(state.selectedTransactionType.count) }
        )
    }
}

@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onSourceClicked: () -> Unit,
    onCategoryClicked: () -> Unit,
    onTagClicked: () -> Unit,
    onPersonClicked: () -> Unit,
    onEvent: (AddTransactionEvent) -> Unit
) {


    val focusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        item {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val listTransactionType =
                        listOf(TransactionType.INCOME, TransactionType.EXPENSE)
                    listTransactionType.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = state.selectedTransactionType == option,
                            onClick = { onEvent(AddTransactionEvent.SelectedType(option)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                        ) {
                            val text = if (option.count == 1) {
                                stringResource(R.string.incoming)
                            } else {
                                stringResource(R.string.outcoming)
                            }
                            FintrackBodyMediumText(text = text)
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            FintrackOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isPrice = true,
                value = state.amount,
                onValueChange = { onEvent(AddTransactionEvent.SetAmount(it)) },
                label = {
                    Row {
                        FintrackBodyMediumText(text = stringResource(R.string.amount))
                        FintrackBodyMediumText(
                            text = stringResource(R.string.required_star),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = state.isAmountError
            )
        }


        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            FintrackOutlinedTextField(
                value = state.category?.second ?: "",
                onClick = onCategoryClicked,
                readOnly = true,
                enabled = false,
                label = {
                    Row {
                        if (state.category?.second != null) {
                            FintrackBodyMediumText(text = stringResource(R.string.category))
                        } else {
                            FintrackBodyMediumText(text = stringResource(R.string.select_category))
                        }
                        FintrackBodyMediumText(
                            text = stringResource(R.string.required_star),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = state.isCategoryError
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            FintrackOutlinedTextField(
                value = state.source?.second ?: "",
                onClick = onSourceClicked,
                readOnly = true,
                enabled = false,
                label = {
                    Row {
                        if (state.source?.second != null) {
                            FintrackBodyMediumText(text = stringResource(R.string.source))
                        } else {
                            FintrackBodyMediumText(text = stringResource(R.string.select_source))
                        }
                        FintrackBodyMediumText(
                            text = stringResource(R.string.required_star),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = state.isSourceError
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            FintrackOutlinedTextField(
                value = state.tags?.joinToString("") { " #${it.second}" } ?: "",
                onClick = onTagClicked,
                readOnly = true,
                enabled = false,
                singleLine = false,
                label = {
                    if (state.tags != null) {
                        FintrackBodyMediumText(text = stringResource(R.string.tags))
                    } else {
                        FintrackBodyMediumText(text = stringResource(R.string.select_tags))
                    }
                }

            )
        }

        item {
            FintrackOutlinedTextField(
                value = state.persons?.joinToString("") { " #${it.second}" } ?: "",
                onClick = onPersonClicked,
                readOnly = true,
                enabled = false,
                singleLine = false,
                label = {
                    if (state.persons != null) {
                        FintrackBodyMediumText(text = stringResource(R.string.persons))
                    } else {
                        FintrackBodyMediumText(text = stringResource(R.string.select_person))
                    }
                }

            )
        }

        /* if (state.tags != null) {
             FlowRow(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.spacedBy(8.dp),
             ) {
                 state.tags.forEach { tag ->
                     FintrackBodySmallText(text = tag.second)
                 }
             }
         }*/

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DatePickerField(
                selectedDate = state.selectedDate,
                onDateSelected = { date, timeStamp ->
                    onEvent(AddTransactionEvent.SetDate(date, timeStamp))
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            FintrackOutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(AddTransactionEvent.SetDescription(it)) },
                label = { FintrackBodyMediumText(text = stringResource(R.string.description)) },
                singleLine = false,
                maxLine = 5
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = { onEvent(AddTransactionEvent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                FintrackTitleMediumText(
                    text = stringResource(R.string.save_transaction),
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}
