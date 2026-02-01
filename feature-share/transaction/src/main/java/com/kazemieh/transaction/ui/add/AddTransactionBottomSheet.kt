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
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.DatePickerField
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.financialsource.ui.list.SourceManageBottomSheet
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: AddTransactionViewModel = koinViewModel(),
    transactionWithRelations: TransactionWithRelations? = null,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    transactionAdded: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(transactionWithRelations) {
        viewModel.onIntent(AddTransactionIntent.FetchDefaultData(transactionWithRelations))
    }

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
                            message = effect.message.asString(context),
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
    snackbarHostState: SnackbarHostState,
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

            if (state.isSourceShow) {
                SourceManageBottomSheet(
                    snackbarHostState = snackbarHostState,
                    onSourceClick = { onIntent(AddTransactionIntent.SetSource(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnSourceClicked) }
                )
            }

            if (state.isSourceEndShow) {
                SourceManageBottomSheet(
                    snackbarHostState = snackbarHostState,
                    onSourceClick = { onIntent(AddTransactionIntent.SetSourceEnd(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnSourceEndClicked) }
                )
            }

            if (state.isCategoryShow) {
                CategoryPickerBottomSheet(
                    transactionType = state.transactionType,
                    snackbarHostState = snackbarHostState,
                    onCategoryClick = { onIntent(AddTransactionIntent.SetCategory(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnCategoryClicked) }
                )
            }

            if (state.isTagShow) {
                TagPickerBottomSheet(
                    snackbarHostState = snackbarHostState,
                    selectedTags = state.tags,
                    onSubmitClick = { onIntent(AddTransactionIntent.SetTags(it)) },
                    onDismiss = { onIntent(AddTransactionIntent.OnTagClicked) }
                )
            }

            if (state.isPersonShow) {
                PersonPickerBottomSheet(
                    snackbarHostState = snackbarHostState,
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
    val space = LocalSpacing.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(space.mediumLarge)
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

        item { Spacer(modifier = Modifier.height(space.large)) }

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

        item { Spacer(modifier = Modifier.height(space.large)) }

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

        item { Spacer(modifier = Modifier.height(space.mediumSmall)) }

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

        item { Spacer(modifier = Modifier.height(space.mediumSmall)) }

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

        item { Spacer(modifier = Modifier.height(space.mediumSmall)) }

        item {
            DatePickerField(
                selectedDate = state.date,
                onDateSelected = { date, timeStamp ->
                    onIntent(AddTransactionIntent.SetDate(date, timeStamp))
                }
            )
        }

        item { Spacer(modifier = Modifier.height(space.mediumSmall)) }

        item {
            FintrackOutlinedTextField(
                value = state.description,
                onValueChange = { onIntent(AddTransactionIntent.SetDescription(it)) },
                label = { FintrackBodyMediumText(text = stringResource(R.string.description)) },
                singleLine = false,
                maxLine = 5
            )
        }

        item { Spacer(modifier = Modifier.height(space.large)) }

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
    val space = LocalSpacing.current
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

        Spacer(modifier = Modifier.height(space.mediumSmall))

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

        Spacer(modifier = Modifier.height(space.mediumSmall))

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
    val space = LocalSpacing.current
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
        Spacer(modifier = Modifier.height(space.mediumSmall))


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
