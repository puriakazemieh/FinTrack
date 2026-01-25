package com.kazemieh.financialsource.ui.delete

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.financialsource.ui.list.SourceListBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSourceBottomSheet(
    viewModel: DeleteSourceViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    source: Source,
    onDismiss: () -> Unit,
    deleted: () -> Unit
) {

    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(source) {
        viewModel.onIntent(DeleteSourceIntent.SetData(source))
    }


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {

                is DeleteSourceEffect.DeletedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            deleted()
                        }
                    }
                }

                is DeleteSourceEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                DeleteSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    BottomSheetContent(
        onIntent = viewModel::onIntent,
        sheetState = sheetState,
        isDeleteAllData = state.isDeleteAllData,
        source = state.moveSource,
        isSourceError = state.isSourceError
    )

    if (state.isSourceListShow) {
        SourceListBottomSheet(
            snackbarHostState = snackbarHostState,
            keyViewmodel = "DeleteSourceBottomSheet",
            onSourceClick = { viewModel.onIntent(DeleteSourceIntent.SetMoveSource(it)) },
            onDismiss = { viewModel.onIntent(DeleteSourceIntent.ShowAllSourceList) }
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    title: String = stringResource(R.string.transaction_delete),
    confirmButtonText: String = stringResource(R.string.confirm),
    dismissButtonText: String = stringResource(R.string.cancell_),
    confirmButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ),
    dismissButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    onIntent: (intent: DeleteSourceIntent) -> Unit,
    isDeleteAllData: Boolean,
    source: Source? = null,
    isSourceError: Boolean = false,
    sheetState: SheetState,
) {

    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = { onIntent(DeleteSourceIntent.Dismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column {
            FintrackTitleLargeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = space.large),
                text = title,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(space.large))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onIntent(DeleteSourceIntent.DeleteAllTransaction)
                }
            ) {
                RadioButton(
                    selected = isDeleteAllData,
                    onClick = { onIntent(DeleteSourceIntent.DeleteAllTransaction) })
                FintrackBodySmallText(text = stringResource(R.string.delete_all_transaction))
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onIntent(DeleteSourceIntent.MoveAllTransaction)
                }
            ) {
                RadioButton(
                    selected = !isDeleteAllData,
                    onClick = { onIntent(DeleteSourceIntent.MoveAllTransaction) })
                FintrackBodySmallText(text = stringResource(R.string.move_to_another_source))
            }

            AnimatedVisibility(
                visible = !isDeleteAllData,
                modifier = Modifier.padding(horizontal = space.mediumSmall)
            ) {
                FintrackOutlinedTextField(
                    value = source?.name ?: "",
                    onClick = { onIntent(DeleteSourceIntent.ShowAllSourceList) },
                    readOnly = true,
                    enabled = false,
                    label = {
                        Row {
                            if (source?.name != null) {
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
                    isError = isSourceError
                )
            }



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(space.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space.mediumSmall)
            ) {
                Button(
                    onClick = { onIntent(DeleteSourceIntent.Submit) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = confirmButtonColors
                ) {
                    FintrackTitleMediumText(
                        text = confirmButtonText,
                        color = MaterialTheme.colorScheme.background
                    )
                }
                Button(
                    onClick = { onIntent(DeleteSourceIntent.Dismiss) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = dismissButtonColors
                ) {
                    FintrackTitleMediumText(
                        text = dismissButtonText,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}
