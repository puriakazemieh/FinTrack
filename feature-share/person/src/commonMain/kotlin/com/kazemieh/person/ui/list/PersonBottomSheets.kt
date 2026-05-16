package com.kazemieh.person.ui.list

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.bottomsheet.ListBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.model.toPerson
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import com.kazemieh.person.ui.delete.DeletePersonBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonPickerBottomSheet(
    viewModel: PersonViewModel = koinViewModel(key = "PersonPickerBottomSheet"),
    snackbarHostState: SnackbarHostState,
    selectedPersons: Set<Person>?,
    onSubmitClick: (Set<Person>?) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.onIntent(PersonIntent.GetAllPerson) }
    LaunchedEffect(selectedPersons) {
        viewModel.onIntent(
            PersonIntent.SetAllSelectedPersons(
                selectedPersons
            )
        )
    }

    val state by viewModel.state.collectAsState()


    SelectableFlowRowBottomSheet(
        title = stringResource(Res.string.persons),
        items = state.items,
        initialSelection = state.initialSelectionIds.map { it.toItemUi() }.toSet(),
        onConfirm = { selectedItems ->
            onSubmitClick(selectedItems.map { it.toPerson() }.toSet())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonManageBottomSheet(
    keyViewmodel: String = "PersonManageBottomSheet",
    viewModel: PersonViewModel = koinViewModel(key = keyViewmodel),
    snackbarHostState: SnackbarHostState,
    isDeleteShow: Boolean = true,
    isEditShow: Boolean = true,
    clickable: Boolean = false,
    onPersonClick: (Person) -> Unit = {},
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.onIntent(PersonIntent.GetAllPerson) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PersonEffect.OnDismiss -> onDismiss()
                is PersonEffect.OnPersonSelected -> onPersonClick(effect.person)
            }
        }
    }

    ListBottomSheet(
        title = stringResource(Res.string.persons),
        items = state.items,
        onItemClicked = { viewModel.onIntent(PersonIntent.SelectedPerson(it.toPerson())) },
        onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
        onDismiss = { viewModel.onIntent(PersonIntent.OnDismiss) },
        isDeleteShow = isDeleteShow,
        isEditShow = isEditShow,
        clickable = clickable,
        onItemEditClicked = { viewModel.onIntent(PersonIntent.OnEditClick(it.toPerson())) },
        onItemDeleteClicked = { viewModel.onIntent(PersonIntent.OnDeleteClick(it.toPerson())) },
    )

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedPerson = state.selectedPerson,
            onDismiss = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
            setPerson = { viewModel.onIntent(PersonIntent.SetSelectedPerson(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedPerson != null) {
        DeletePersonBottomSheet(
            snackbarHostState = snackbarHostState,
            person = state.selectedPerson!!,
            onDismiss = { viewModel.onIntent(PersonIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(PersonIntent.OnDeleteClick()) }
        )
    }
}

@Composable
fun PersonSelectionBottomSheet(
    viewModel: PersonViewModel = koinViewModel(key = "PersonSelectionBottomSheet"),
    initialSelectionPairs: Set<Person> = emptySet(),
    isAllSelected: Boolean = true,
    onConfirmPairs: (Set<Person>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) { viewModel.onIntent(PersonIntent.GetAllPerson) }

    val initialSelectionIds = if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(Res.string.persons),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toPerson() }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}
