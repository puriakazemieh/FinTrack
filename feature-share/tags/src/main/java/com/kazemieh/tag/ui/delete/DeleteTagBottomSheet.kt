package com.kazemieh.tag.ui.delete

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
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.tag.ui.list.TagListBottomSheet
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

    val state by viewModel.state.collectAsState()

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

    BottomSheetContent(
        onIntent = viewModel::onIntent,
        sheetState = sheetState,
        isDeleteAllData = state.isDeleteAllData,
        tag = state.moveTag,
        isCategoryError = state.isTagError
    )

    if (state.isTagListShow) {
        TagListBottomSheet(
            keyViewmodel = "DeleteTagBottomSheet",
            snackbarHostState = snackbarHostState,
            isEditShow = false,
            isDeleteShow = false,
            clickable = true,
            onTagClick = { viewModel.onIntent(DeleteTagIntent.SetMoveTag(it)) },
            onDismiss = { viewModel.onIntent(DeleteTagIntent.ShowAllTagList) }
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
    onIntent: (intent: DeleteTagIntent) -> Unit,
    isDeleteAllData: Boolean,
    tag: Tag? = null,
    isCategoryError: Boolean = false,
    sheetState: SheetState,
) {

    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = { onIntent(DeleteTagIntent.Dismiss) },
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
                    onIntent(DeleteTagIntent.DeleteAllTransaction)
                }
            ) {
                RadioButton(
                    selected = isDeleteAllData,
                    onClick = { onIntent(DeleteTagIntent.DeleteAllTransaction) })
                FintrackBodySmallText(text = stringResource(R.string.delete_all_transaction))
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onIntent(DeleteTagIntent.MoveAllTransaction)
                }
            ) {
                RadioButton(
                    selected = !isDeleteAllData,
                    onClick = { onIntent(DeleteTagIntent.MoveAllTransaction) })
                FintrackBodySmallText(text = stringResource(R.string.move_to_another_category))
            }

            AnimatedVisibility(
                visible = !isDeleteAllData,
                modifier = Modifier.padding(horizontal = space.mediumSmall)
            ) {
                FintrackOutlinedTextField(
                    value = tag?.name ?: "",
                    onClick = { onIntent(DeleteTagIntent.ShowAllTagList) },
                    readOnly = true,
                    enabled = false,
                    label = {
                        Row {
                            if (tag?.name != null) {
                                FintrackBodyMediumText(text = stringResource(R.string.tags))
                            } else {
                                FintrackBodyMediumText(text = stringResource(R.string.select_category))
                            }
                            FintrackBodyMediumText(
                                text = stringResource(R.string.required_star),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = isCategoryError
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
                    onClick = { onIntent(DeleteTagIntent.Submit) },
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
                    onClick = { onIntent(DeleteTagIntent.Dismiss) },
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
