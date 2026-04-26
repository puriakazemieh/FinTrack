package com.kazemieh.tag.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.tag.ui.list.TagManageBottomSheet
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTagBottomSheet(
    viewModel: DeleteTagViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    tag: Tag,
    onDismiss: () -> Unit,
    deleted: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tag) {
        viewModel.onIntent(DeleteTagIntent.SetData(tag))
    }


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {

                is DeleteTagEffect.DeletedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            deleted()
                        }
                    }
                }

                is DeleteTagEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message.resolveString(),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                DeleteTagEffect.OnDismiss -> onDismiss()
            }
        }
    }


    DeleteWithMoveBottomSheetContent(
        sheetState = sheetState,
        title = stringResource(Res.string.transaction_delete),
        deleteAllText = stringResource(Res.string.delete_all_transaction),

        moveToAnotherText = stringResource(Res.string.move_to_another_tag),
        targetTitleText = stringResource(Res.string.tags),
        targetPlaceholderText = stringResource(Res.string.select_tag),

        targetValue = state.moveTag?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isTagError,

        onSelectDeleteAll = { viewModel.onIntent(DeleteTagIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeleteTagIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeleteTagIntent.ShowAllTagList) },
        onConfirm = { viewModel.onIntent(DeleteTagIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeleteTagIntent.Dismiss) },

        confirmButtonText = stringResource(Res.string.confirm),
        dismissButtonText = stringResource(Res.string.cancell_),
    )

    if (state.isTagListShow) {
        TagManageBottomSheet(
            snackbarHostState = snackbarHostState,
            isEditShow = false,
            isDeleteShow = false,
            clickable = true,
            onTagClick = { viewModel.onIntent(DeleteTagIntent.SetMoveTag(it)) },
            onDismiss = { viewModel.onIntent(DeleteTagIntent.ShowAllTagList) }
        )
    }

}
