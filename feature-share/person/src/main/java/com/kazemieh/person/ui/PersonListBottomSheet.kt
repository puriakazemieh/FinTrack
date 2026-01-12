package com.kazemieh.person.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.toItemUi
import com.kazemieh.common.model.toPerson
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.list.selectable.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonListBottomSheet(
    viewModel: PersonViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedPersons: Set<Person>?,
    onSubmitClick: (Set<Person>?) -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(true) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    LaunchedEffect(selectedPersons) {
        viewModel.onIntent(PersonIntent.SetAllSelectedPersons(selectedPersons))
    }

    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    SelectableFlowRowBottomSheet(
        title = stringResource(R.string.persons),
        items = state.items,
        initialSelection = state.initialSelectionIds.map { it.toItemUi() }.toSet(),
        onConfirm = { selectedItems ->
            onSubmitClick(selectedItems.map { it.toPerson(context) }.toSet())
        },
        onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
        onDismiss = onDismiss,
    )

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            snackbarHostState = snackbarHostState,
            onDismiss = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
            setPerson = { viewModel.onIntent(PersonIntent.SetSelectedPerson(it)) }
        )
    }
}


@Composable
fun PersonListSelectionBottomSheet(
    viewModel: PersonViewModel = koinViewModel(),
    initialSelectionPairs: Set<Person> = emptySet(),
    onConfirmPairs: (Set<Person>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    val initialSelectionIds = initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.persons),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toPerson(context) }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}