package com.kazemieh.financialsource.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.financialsource.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceBottomSheet(
    viewModel: AddFinancialSourceViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    setSource: (id: Int, name: String) -> Unit
) {

    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddFinancialSourceEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is AddFinancialSourceEffect.AddedFinancialSource ->
                    setSource(effect.id, effect.name)

                AddFinancialSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddFinancialSourceIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectedTypeFinancialSource.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = state.selectedTypeFinancialSource == option,
                                onClick = {
                                    viewModel.onIntent(AddFinancialSourceIntent.SelectedType(option))
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                            ) {
                                FintrackBodyMediumText(text = stringResource(option.value))
                            }
                        }
                    }
                }
                // نام منبع
                FintrackOutlinedTextField(
                    value = state.sourceName ?: "",
                    onValueChange = { viewModel.onIntent(AddFinancialSourceIntent.SetSourceName(it)) },
                    label = {
                        Row {
                            FintrackBodyMediumText(text = stringResource(R.string.source_name_label))
                            FintrackBodyMediumText(
                                text = stringResource(R.string.required_star),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // مبلغ اولیه
                FintrackOutlinedTextField(
                    value = if (state.balance == 0) "" else state.balance.toString(),
                    onValueChange = { input ->
                        val newValue = input.toIntOrNull()
                        if (newValue != null) viewModel.onIntent(
                            AddFinancialSourceIntent.SetBalance(newValue)
                        )
                        else if (input.isEmpty()) viewModel.onIntent(
                            AddFinancialSourceIntent.SetBalance(0)
                        )
                    },
                    label = { FintrackBodyMediumText(text = stringResource(R.string.initial_balance_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // شماره کارت (در صورت CREDIT)
                if (state.selectedTypeFinancialSource == SelectedTypeFinancialSource.CREDIT) {
                    FintrackOutlinedTextField(
                        value = state.cardNumber ?: "",
                        onValueChange = {
                            viewModel.onIntent(
                                AddFinancialSourceIntent.SetCardNumber(it)
                            )
                        },
                        label = { FintrackBodyMediumText(text = stringResource(R.string.card_number_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // توضیحات
                FintrackOutlinedTextField(
                    value = state.description ?: "",
                    onValueChange = { viewModel.onIntent(AddFinancialSourceIntent.SetDescription(it)) },
                    label = { FintrackBodyMediumText(text = stringResource(R.string.description_label)) }
                )

                Spacer(Modifier.height(16.dp))

                // دکمه ثبت
                Button(
                    onClick = { viewModel.onIntent(AddFinancialSourceIntent.AddFinancialSource) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(
                        text = stringResource(R.string.submit_source),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }

            // Snackbar
            Box {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

