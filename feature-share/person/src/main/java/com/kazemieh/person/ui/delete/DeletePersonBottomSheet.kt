package com.kazemieh.person.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.person.ui.list.PersonManageBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletePersonBottomSheet(
    viewModel: DeletePersonViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    person: Person,
    onDismiss: () -> Unit,
    deleted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(person) {
        viewModel.onIntent(DeletePersonIntent.SetData(person))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DeletePersonEffect.DeletedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) deleted()
                    }
                }
                is DeletePersonEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                DeletePersonEffect.OnDismiss -> onDismiss()
            }
        }
    }

    DeleteWithMoveBottomSheetContent(
        sheetState = sheetState,
        title = stringResource(R.string.transaction_delete),
        deleteAllText = stringResource(R.string.delete_all_transaction),
        moveToAnotherText = stringResource(R.string.move_to_another_person),
        targetTitleText = stringResource(R.string.person_name_label),
        targetPlaceholderText = stringResource(R.string.select_person),
        targetValue = state.movePerson?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isPersonError,
        onSelectDeleteAll = { viewModel.onIntent(DeletePersonIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeletePersonIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeletePersonIntent.ShowAllPersonList) },
        onConfirm = { viewModel.onIntent(DeletePersonIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeletePersonIntent.Dismiss) },
        confirmButtonText = stringResource(R.string.confirm),
        dismissButtonText = stringResource(R.string.cancell_),
    )

    if (state.isPersonListShow) {
        PersonManageBottomSheet(
            snackbarHostState = snackbarHostState,
            isEditShow = false,
            isDeleteShow = false,
            clickable = true,
            onPersonClick = { viewModel.onIntent(DeletePersonIntent.SetMovePerson(it)) }, // TODO
            onDismiss = { viewModel.onIntent(DeletePersonIntent.ShowAllPersonList) }
        )
    }
}
