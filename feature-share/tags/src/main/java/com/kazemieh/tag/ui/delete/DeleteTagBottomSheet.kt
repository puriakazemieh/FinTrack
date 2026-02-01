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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.tag.ui.list.TagManageBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


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

    val context = LocalContext.current

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
                            message = context.getString(effect.message),
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
        title = stringResource(R.string.transaction_delete),
        deleteAllText = stringResource(R.string.delete_all_transaction),

        moveToAnotherText = stringResource(R.string.move_to_another_tag),
        targetTitleText = stringResource(R.string.tags),
        targetPlaceholderText = stringResource(R.string.select_tag),

        targetValue = state.moveTag?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isTagError,

        onSelectDeleteAll = { viewModel.onIntent(DeleteTagIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeleteTagIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeleteTagIntent.ShowAllTagList) },
        onConfirm = { viewModel.onIntent(DeleteTagIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeleteTagIntent.Dismiss) },

        confirmButtonText = stringResource(R.string.confirm),
        dismissButtonText = stringResource(R.string.cancell_),
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
