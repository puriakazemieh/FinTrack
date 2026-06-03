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
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.person.ui.list.PersonManageBottomSheet
import com.kazemieh.person.ui.list.PersonPickerSingleBottomSheet
import kotlinx.coroutines.launch
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
                            message = effect.message.resolveString(),
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
        itemName = person.name,
        itemType = stringResource(Res.string.person_name),
        deleteAllText = stringResource(Res.string.delete_all_transaction),
        moveToAnotherText = stringResource(Res.string.move_to_another_person),
        targetPlaceholderText = stringResource(Res.string.select_person),
        targetValue = state.movePerson?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isPersonError,
        onSelectDeleteAll = { viewModel.onIntent(DeletePersonIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeletePersonIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeletePersonIntent.ShowAllPersonList) },
        onConfirm = { viewModel.onIntent(DeletePersonIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeletePersonIntent.Dismiss) },
        confirmButtonText = stringResource(Res.string.confirm),
        dismissButtonText = stringResource(Res.string.cancell_),
    )

    if (state.isPersonListShow) {
        PersonPickerSingleBottomSheet(
            snackbarHostState = snackbarHostState,
            onPersonClick = { viewModel.onIntent(DeletePersonIntent.SetMovePerson(it)) },
            onDismiss = { viewModel.onIntent(DeletePersonIntent.ShowAllPersonList) }
        )
    }
}
