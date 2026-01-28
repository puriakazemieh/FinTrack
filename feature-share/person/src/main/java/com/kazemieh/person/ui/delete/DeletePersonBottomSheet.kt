package com.kazemieh.person.ui.delete

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
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.person.ui.list.PersonListBottomSheet
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

    val state by viewModel.state.collectAsState()

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
                        if (!sheetState.isVisible) {
                            deleted()
                        }
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

    BottomSheetContent(
        onIntent = viewModel::onIntent,
        sheetState = sheetState,
        isDeleteAllData = state.isDeleteAllData,
        person = state.movePerson,
        isCategoryError = state.isPersonError
    )

    if (state.isPersonListShow) {
        PersonListBottomSheet(
            keyViewmodel = "DeletePersonBottomSheet",
            snackbarHostState = snackbarHostState,
            isEditShow = false,
            isDeleteShow = false,
            clickable = true,
            onPersonClick = { viewModel.onIntent(DeletePersonIntent.SetMovePerson(it)) },
            onDismiss = { viewModel.onIntent(DeletePersonIntent.ShowAllPersonList) }
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
    onIntent: (intent: DeletePersonIntent) -> Unit,
    isDeleteAllData: Boolean,
    person: Person? = null,
    isCategoryError: Boolean = false,
    sheetState: SheetState,
) {

    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = { onIntent(DeletePersonIntent.Dismiss) },
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
                    onIntent(DeletePersonIntent.DeleteAllTransaction)
                }
            ) {
                RadioButton(
                    selected = isDeleteAllData,
                    onClick = { onIntent(DeletePersonIntent.DeleteAllTransaction) })
                FintrackBodySmallText(text = stringResource(R.string.delete_all_transaction))
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onIntent(DeletePersonIntent.MoveAllTransaction)
                }
            ) {
                RadioButton(
                    selected = !isDeleteAllData,
                    onClick = { onIntent(DeletePersonIntent.MoveAllTransaction) })
                FintrackBodySmallText(text = stringResource(R.string.move_to_another_category))
            }

            AnimatedVisibility(
                visible = !isDeleteAllData,
                modifier = Modifier.padding(horizontal = space.mediumSmall)
            ) {
                FintrackOutlinedTextField(
                    value = person?.name ?: "",
                    onClick = { onIntent(DeletePersonIntent.ShowAllPersonList) },
                    readOnly = true,
                    enabled = false,
                    label = {
                        Row {
                            if (person?.name != null) {
                                FintrackBodyMediumText(text = stringResource(R.string.persons))
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
                    onClick = { onIntent(DeletePersonIntent.Submit) },
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
                    onClick = { onIntent(DeletePersonIntent.Dismiss) },
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
