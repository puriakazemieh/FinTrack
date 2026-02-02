package com.kazemieh.financialsource.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
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
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                        if (!sheetState.isVisible) deleted()
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

    DeleteWithMoveBottomSheetContent(
        sheetState = sheetState,
        title = stringResource(R.string.transaction_delete),
        deleteAllText = stringResource(R.string.delete_all_transaction),
        moveToAnotherText = stringResource(R.string.move_to_another_source),
        targetTitleText = stringResource(R.string.source_name_label),
        targetPlaceholderText = stringResource(R.string.select_source),
        targetValue = state.moveSource?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isSourceError,
        onSelectDeleteAll = { viewModel.onIntent(DeleteSourceIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeleteSourceIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeleteSourceIntent.ShowAllSourceList) },
        onConfirm = { viewModel.onIntent(DeleteSourceIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeleteSourceIntent.Dismiss) },
        confirmButtonText = stringResource(R.string.confirm),
        dismissButtonText = stringResource(R.string.cancell_),
    )

    if (state.isSourceListShow) {
        SourcePickerBottomSheet(
            snackbarHostState = snackbarHostState,
            onSourceClick = { viewModel.onIntent(DeleteSourceIntent.SetMoveSource(it)) },
            onDismiss = { viewModel.onIntent(DeleteSourceIntent.ShowAllSourceList) }
        )
    }
}
