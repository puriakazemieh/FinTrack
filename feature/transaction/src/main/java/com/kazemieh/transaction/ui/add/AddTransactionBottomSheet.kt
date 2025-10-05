package com.kazemieh.transaction.ui.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.model.TransactionType
import com.kazemieh.transaction.R
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
    onCategoryClicked: (Int) -> Unit,
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
                onCategoryClicked = { onCategoryClicked(state.selectedTransactionType.count) }
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
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            TransactionType.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = state.selectedTransactionType == option,
                    onClick = { onEvent(AddTransactionEvent.SelectedType(option)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
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

        Spacer(modifier = Modifier.height(16.dp))

        FintrackOutlinedTextField(
            value = state.amount,
            onValueChange = { onEvent(AddTransactionEvent.SetAmount(it)) },
            label = { FintrackBodyMediumText(text = stringResource(R.string.amount)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        FintrackOutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(AddTransactionEvent.SetDescription(it)) },
            label = { FintrackBodyMediumText(text = stringResource(R.string.description)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Selector(
            label = stringResource(R.string.select_category),
            item = state.category?.second ?: stringResource(R.string.select_category),
            onClicked = onCategoryClicked,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Selector(
            label = stringResource(R.string.select_source),
            item = state.source?.second ?: stringResource(R.string.select_source),
            onClicked = onSourceClicked,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.tags != null) {
            Column(Modifier.fillMaxWidth()) {

                Selector(
                    label = stringResource(R.string.select_tags),
                    item = stringResource(R.string.change),
                    onClicked = onTagClicked,
                )

                Spacer(Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.tags.forEach { tag ->
                        TextButton(onClick = { /* هیچ عملی */ }) {
                            FintrackBodySmallText(text = tag.second)
                        }
                    }
                }
            }
        } else {
            Selector(
                label = stringResource(R.string.select_tags),
                item = stringResource(R.string.tag_list),
                onClicked = onTagClicked,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DatePickerField(
            selectedDate = state.selectedDate,
            onDateSelected = { onEvent(AddTransactionEvent.SetDate(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(AddTransactionEvent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            FintrackTitleMediumText(text = stringResource(R.string.save_transaction))
        }
    }
}
