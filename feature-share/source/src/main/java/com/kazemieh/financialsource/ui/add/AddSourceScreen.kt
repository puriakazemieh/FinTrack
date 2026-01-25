package com.kazemieh.financialsource.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceBottomSheet(
    viewModel: AddFinancialSourceViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedSource: Source? = null,
    onDismiss: () -> Unit,
    setSource: (Source) -> Unit
) {

    val space = LocalSpacing.current
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        if (selectedSource != null)
            viewModel.onIntent(AddSourceIntent.ShowEditData(selectedSource))
    }

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

                is AddFinancialSourceEffect.AddedFinancialSource -> setSource(effect.source)

                AddFinancialSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddSourceIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(space.mediumLarge)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TypeSource.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = state.typeSource == option,
                            onClick = {
                                viewModel.onIntent(AddSourceIntent.SelectedType(option))
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            FintrackBodyMediumText(text = stringResource(option.value))
                        }
                    }
                }
            }

            FintrackOutlinedTextField(
                value = state.sourceName ?: "",
                onValueChange = { viewModel.onIntent(AddSourceIntent.SetSourceName(it)) },
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

            FintrackOutlinedTextField(
                isPrice = true,
                value = if (state.balance == 0) "" else state.balance.toString(),
                onValueChange = { input ->
                    val newValue = input.toIntOrNull()
                    if (newValue != null) viewModel.onIntent(
                        AddSourceIntent.SetBalance(newValue)
                    )
                    else if (input.isEmpty()) viewModel.onIntent(
                        AddSourceIntent.SetBalance(0)
                    )
                },
                label = { FintrackBodyMediumText(text = stringResource(R.string.initial_balance_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )


            AnimatedVisibility(visible = state.typeSource == TypeSource.CREDIT) {
                FintrackOutlinedTextField(
                    value = state.cardNumber ?: "",
                    onValueChange = {
                        viewModel.onIntent(
                            AddSourceIntent.SetCardNumber(it)
                        )
                    },
                    label = { FintrackBodyMediumText(text = stringResource(R.string.card_number_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            FintrackOutlinedTextField(
                value = state.description ?: "",
                onValueChange = { viewModel.onIntent(AddSourceIntent.SetDescription(it)) },
                label = { FintrackBodyMediumText(text = stringResource(R.string.description_label)) }
            )

            Spacer(Modifier.height(space.large))


            Button(
                onClick = { viewModel.onIntent(AddSourceIntent.AddSource) },
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
    }
}

