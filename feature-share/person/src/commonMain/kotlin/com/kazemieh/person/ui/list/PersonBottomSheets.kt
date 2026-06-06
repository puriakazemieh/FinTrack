package com.kazemieh.person.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.component.bottomsheet.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.model.toPerson
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import com.kazemieh.person.ui.delete.DeletePersonBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_person_count
import fintrack.core.designsystem.generated.resources.person_item
import fintrack.core.designsystem.generated.resources.persons
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
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            setPerson = { viewModel.onIntent(PersonIntent.SetSelectedPerson(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonPickerSingleBottomSheet(
    viewModel: PersonViewModel = koinViewModel(key = "PersonPickerSingleBottomSheet"),
    snackbarHostState: SnackbarHostState,
    onPersonClick: (Person) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PersonEffect.OnPersonSelected -> onPersonClick(effect.person)
                PersonEffect.OnDismiss -> onDismiss()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(PersonIntent.ResetFlags)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = stringResource(Res.string.persons),
                onClose = {
                    viewModel.onIntent(PersonIntent.ResetFlags)
                    onDismiss()
                }
            )

            EntityList(
                title = stringResource(Res.string.persons),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(PersonIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
                items = state.filteredPersons.map {
                    EntityItem(
                        id = it.id ?: 0,
                        name = it.name,
                        sub = it.description,
                        iconId = 1,
                        colorId = 1
                    )
                },
                onEditClick = { item ->
                    state.persons.find { it.id == item.id }?.let {
                        viewModel.onIntent(PersonIntent.OnEditClick(it))
                    }
                },
                onDeleteClick = { item ->
                    state.persons.find { it.id == item.id }?.let {
                        viewModel.onIntent(PersonIntent.OnDeleteClick(it))
                    }
                },
                onItemClick = { item ->
                    state.persons.find { it.id == item.id }?.let { onPersonClick(it) }
                }
            )
        }
    }

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedPerson = state.selectedPerson,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(PersonIntent.ResetFlags)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = stringResource(Res.string.persons),
                onClose = {
                    viewModel.onIntent(PersonIntent.ResetFlags)
                    onDismiss()
                }
            )

            EntityList(
                title = stringResource(Res.string.persons),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(PersonIntent.UpdateSearchQuery(it)) },
                items = state.filteredPersons.map { person ->
                    EntityItem(
                        id = person.id ?: 0L,
                        name = person.name,
                        sub = person.description,
                        iconId = 1, // Default user icon? Or handle properly
                        colorId = 1
                    )
                },
                onItemClick = { item ->
                    if (clickable) {
                        state.persons.find { it.id == item.id }?.let {
                            viewModel.onIntent(PersonIntent.SelectedPerson(it))
                        }
                    }
                },
                onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
                onEditClick = { item ->
                    state.persons.find { it.id == item.id }?.let {
                        viewModel.onIntent(PersonIntent.OnEditClick(it))
                    }
                },
                onDeleteClick = { item ->
                    state.persons.find { it.id == item.id }?.let {
                        viewModel.onIntent(PersonIntent.OnDeleteClick(it))
                    }
                }
            )
        }
    }

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedPerson = state.selectedPerson,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            setPerson = { viewModel.onIntent(PersonIntent.SelectedPerson(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedPerson != null) {
        DeletePersonBottomSheet(
            snackbarHostState = snackbarHostState,
            person = state.selectedPerson!!,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            deleted = { viewModel.onIntent(PersonIntent.ResetFlags) }
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

    val initialSelectionIds =
        if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(Res.string.persons),
        items = state.items,
        initialSelection = initialSelectionIds,
        query = state.searchQuery,
        onQueryChange = { viewModel.onIntent(PersonIntent.UpdateSearchQuery(it)) },
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toPerson() }.toSet(), isAll)
            viewModel.onIntent(PersonIntent.OnDismiss)
        },
        onDismiss = {
            viewModel.onIntent(PersonIntent.OnDismiss)
        }
    )
}
