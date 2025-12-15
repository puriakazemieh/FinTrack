package com.kazemieh.transaction.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.kazemieh.category.ui.CategoryListBottomSheet
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.DatePickerField
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.financialsource.ui.SourceListBottomSheet
import com.kazemieh.person.ui.PersonListBottomSheet
import com.kazemieh.tag.ui.TagListBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: AddTransactionViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    transactionAdded: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        viewModel.onIntent(AddTransactionIntent.FetchDefaultData)
    }
    /*    LaunchedEffect(source) {
            viewModel.onIntent(AddTransactionIntent.SetSource(source))
        }
        LaunchedEffect(sourceEnd) {
            viewModel.onIntent(AddTransactionIntent.SetSourceEnd(sourceEnd))
        }
        LaunchedEffect(category) {
            viewModel.onIntent(AddTransactionIntent.SetCategory(category))
        }
        LaunchedEffect(tags) {
            viewModel.onIntent(AddTransactionIntent.SetTags(tags))
        }
        LaunchedEffect(persons) {
            viewModel.onIntent(AddTransactionIntent.SetPerson(persons))
        }*/

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTransactionEffect.AddedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            transactionAdded()
                            onDismiss()
                        }
                    }
                }

                is AddTransactionEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                AddTransactionEffect.OnDismiss -> onDismiss()
            }
        }
    }

    BottomSheetContent(state, viewModel::onIntent, sheetState, snackbarHostState)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    state: AddTransactionState,
    onIntent: (intent: AddTransactionIntent) -> Unit,
    sheetState: SheetState,
    snackbarHostState: SnackbarHostState
) {
    ModalBottomSheet(
        onDismissRequest = { onIntent(AddTransactionIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {

            AddTransactionContent(state = state, onIntent = onIntent)

            Box {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (state.isSourceShow) {
                SourceListBottomSheet(
                    onSourceClick = { onIntent(AddTransactionIntent.SetSource(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnSourceClicked) }
                )
            }

            if (state.isSourceEndShow) {
                SourceListBottomSheet(
                    onSourceClick = { onIntent(AddTransactionIntent.SetSourceEnd(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnSourceEndClicked) }
                )
            }

            if (state.isCategoryShow) {
                CategoryListBottomSheet(
                    transactionType = state.transactionType,
                    onCategoryClick = { onIntent(AddTransactionIntent.SetCategory(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnCategoryClicked) }
                )
            }

            if (state.isTagShow) {
                TagListBottomSheet(
                    selectedTags = state.tags,
                    onSubmitClick = { onIntent(AddTransactionIntent.SetTags(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnTagClicked) }
                )
            }

            if (state.isPersonShow) {
                PersonListBottomSheet(
                    selectedPersons = state.persons,
                    onSubmitClick = { onIntent(AddTransactionIntent.SetPerson(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnPersonClicked) }
                )
            }
        }
    }
}

@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onIntent: (AddTransactionIntent) -> Unit
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
                    state.listTransactionType.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = state.transactionType == option,
                            onClick = { onIntent(AddTransactionIntent.SelectedType(option)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = state.listTransactionType.size
                            ),
                        ) {
                            val text = when (option.count) {
                                1 -> {
                                    stringResource(R.string.incoming)
                                }

                                2 -> {
                                    stringResource(R.string.outcoming)
                                }

                                else -> {
                                    stringResource(R.string.transfer)
                                }
                            }

                            FintrackBodyMediumText(text = text)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            FintrackOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isPrice = true,
                value = state.amount,
                onValueChange = { onIntent(AddTransactionIntent.SetAmount(it)) },
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

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            AnimatedVisibility(visible = state.transactionType == TransactionType.TRANSFER) {
                TransferScreen(
                    state = state,
                    onSourceClicked = { onIntent(AddTransactionIntent.OnSourceClicked) },
                    onSourceEndClicked = { onIntent(AddTransactionIntent.OnSourceEndClicked) },
                    setTransferAmount = { onIntent(AddTransactionIntent.SetAmountTransfer(it)) }
                )
            }
        }

        item {
            AnimatedVisibility(visible = state.transactionType != TransactionType.TRANSFER) {
                TransactionDetail(
                    state = state,
                    onSourceClicked = { onIntent(AddTransactionIntent.OnSourceClicked) },
                    onCategoryClicked = { onIntent(AddTransactionIntent.OnCategoryClicked) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            FintrackOutlinedTextField(
                value = state.tags?.joinToString("") { " #${it.name}" } ?: "",
                onClick = { onIntent(AddTransactionIntent.OnTagClicked) },
                readOnly = true,
                enabled = false,
                singleLine = false,
                label = {
                    if (!state.tags.isNullOrEmpty()) {
                        FintrackBodyMediumText(text = stringResource(R.string.tags))
                    } else {
                        FintrackBodyMediumText(text = stringResource(R.string.select_tags))
                    }
                }

            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            FintrackOutlinedTextField(
                value = state.persons?.joinToString("") { " #${it.name}" } ?: "",
                onClick = { onIntent(AddTransactionIntent.OnPersonClicked) },
                readOnly = true,
                enabled = false,
                singleLine = false,
                label = {
                    if (!state.persons.isNullOrEmpty()) {
                        FintrackBodyMediumText(text = stringResource(R.string.persons))
                    } else {
                        FintrackBodyMediumText(text = stringResource(R.string.select_person))
                    }
                }

            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            DatePickerField(
                selectedDate = state.date,
                onDateSelected = { date, timeStamp ->
                    onIntent(AddTransactionIntent.SetDate(date, timeStamp))
                }
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            FintrackOutlinedTextField(
                value = state.description,
                onValueChange = { onIntent(AddTransactionIntent.SetDescription(it)) },
                label = { FintrackBodyMediumText(text = stringResource(R.string.description)) },
                singleLine = false,
                maxLine = 5
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Button(
                onClick = { onIntent(AddTransactionIntent.Submit) },
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

@Composable
private fun TransferScreen(
    state: AddTransactionState,
    onSourceClicked: () -> Unit,
    onSourceEndClicked: () -> Unit,
    setTransferAmount: (String) -> Unit,
) {
    Column {
        FintrackOutlinedTextField(
            value = state.source?.name ?: "",
            onClick = onSourceClicked,
            readOnly = true,
            enabled = false,
            label = {
                Row {
                    if (state.source?.name != null) {
                        FintrackBodyMediumText(text = stringResource(R.string.source_from))
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

        Spacer(modifier = Modifier.height(8.dp))

        FintrackOutlinedTextField(
            value = state.sourceEnd?.name ?: "",
            onClick = onSourceEndClicked,
            readOnly = true,
            enabled = false,
            label = {
                Row {
                    if (state.sourceEnd?.name != null) {
                        FintrackBodyMediumText(text = stringResource(R.string.source_to))
                    } else {
                        FintrackBodyMediumText(text = stringResource(R.string.select_source))
                    }
                    FintrackBodyMediumText(
                        text = stringResource(R.string.required_star),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = state.isSourceEndError
        )

        Spacer(modifier = Modifier.height(8.dp))

        FintrackOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            isPrice = true,
            value = state.amountTransfer ?: "0",
            onValueChange = { setTransferAmount(it) },
            label = {
                Row {
                    FintrackBodyMediumText(text = stringResource(R.string.amount_transfer))
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )

    }
}

@Composable
private fun TransactionDetail(
    state: AddTransactionState,
    onSourceClicked: () -> Unit,
    onCategoryClicked: () -> Unit,
) {
    Column {
        FintrackOutlinedTextField(
            value = state.category?.name ?: "",
            onClick = onCategoryClicked,
            readOnly = true,
            enabled = false,
            label = {
                Row {
                    if (state.category?.name != null) {
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
        Spacer(modifier = Modifier.height(8.dp))


        FintrackOutlinedTextField(
            value = state.source?.name ?: "",
            onClick = onSourceClicked,
            readOnly = true,
            enabled = false,
            label = {
                Row {
                    if (state.source?.name != null) {
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
}
