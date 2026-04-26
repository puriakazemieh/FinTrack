package com.kazemieh.financialsource.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import fintrack.core.designsystem.generated.resources.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
                            message = effect.message.resolveString(),
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
        title = stringResource(Res.string.transaction_delete),
        deleteAllText = stringResource(Res.string.delete_all_transaction),
        moveToAnotherText = stringResource(Res.string.move_to_another_source),
        targetTitleText = stringResource(Res.string.source_name_label),
        targetPlaceholderText = stringResource(Res.string.select_source),
        targetValue = state.moveSource?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isSourceError,
        onSelectDeleteAll = { viewModel.onIntent(DeleteSourceIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeleteSourceIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeleteSourceIntent.ShowAllSourceList) },
        onConfirm = { viewModel.onIntent(DeleteSourceIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeleteSourceIntent.Dismiss) },
        confirmButtonText = stringResource(Res.string.confirm),
        dismissButtonText = stringResource(Res.string.cancell_),
    )

    if (state.isSourceListShow) {
        SourcePickerBottomSheet(
            snackbarHostState = snackbarHostState,
            onSourceClick = { viewModel.onIntent(DeleteSourceIntent.SetMoveSource(it)) },
            onDismiss = { viewModel.onIntent(DeleteSourceIntent.ShowAllSourceList) }
        )
    }
}
