package com.kazemieh.person.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import com.kazemieh.designsystem.GlassGreen
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.bottomsheet.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.model.toPerson
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import com.kazemieh.person.ui.delete.DeletePersonBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.persons
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonPickerBottomSheet(
    viewModel: PersonViewModel = koinViewModel(key = "PersonPickerBottomSheet"),
    selectedPersons: Set<Person>?,
    onSubmitClick: (Set<Person>?) -> Unit,
    onDismiss: () -> Unit,
    isEditShow: Boolean = false,
    showEditButton: Boolean = true
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
        isEditShow = isEditShow,
        showEditButton = showEditButton,
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

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            selectedPerson = state.selectedPerson,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            onNavigateToTransactions = null, // Picker usually doesn't need filter navigation
            setPerson = { viewModel.onIntent(PersonIntent.SetSelectedPerson(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedPerson != null) {
        DeletePersonBottomSheet(
            person = state.selectedPerson!!,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            deleted = { viewModel.onIntent(PersonIntent.ResetFlags) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonPickerSingleBottomSheet(
    viewModel: PersonViewModel = koinViewModel(key = "PersonPickerSingleBottomSheet"),
    onPersonClick: (Person) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Person) -> Unit)? = null,
    isEditShow: Boolean = false,
    showEditButton: Boolean = true
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    val state by viewModel.state.collectAsState()
    var isEditMode by remember { mutableStateOf(isEditShow) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PersonEffect.OnPersonSelected -> onPersonClick(effect.person)
                PersonEffect.OnDismiss -> onDismiss()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glassColors = LocalGlassColors.current

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(PersonIntent.ResetFlags)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ScreenHeader(
                    title = stringResource(Res.string.persons),
                    onClose = {
                        viewModel.onIntent(PersonIntent.ResetFlags)
                        onDismiss()
                    },
                    trailingContent = {
                            IconButton(onClick = { viewModel.onIntent(PersonIntent.OnToggleReorder) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    tint = if (state.isReorderShow) GlassGreen else LocalGlassColors.current.text2
                                )
                            }
                            if (showEditButton) {
                                TextButton(onClick = { isEditMode = !isEditMode }) {
                                    FintrackLabelMediumText(
                                        text = if (isEditMode) stringResource(Res.string.cancell_) else stringResource(Res.string.edit)
                                    )
                                }
                            }
                    }
                )

                EntityList(
                    title = stringResource(Res.string.persons),
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onIntent(PersonIntent.UpdateSearchQuery(it)) },
                    onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
                    showActions = isEditMode && !state.isReorderShow,
                    isReorderMode = state.isReorderShow,
                    onMove = { from, to ->
                        val list = state.persons.toMutableList()
                        list.add(to, list.removeAt(from))
                        val positions = list.mapIndexed { index, person ->
                            person.id!! to index
                        }.toMap()
                        viewModel.onIntent(PersonIntent.UpdatePositions(positions))
                    },
                    onFilterClick = onNavigateToTransactions?.let { callback ->
                        { item ->
                            state.persons.find { it.id == item.id }?.let { callback(it) }
                            onDismiss()
                        }
                    },
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
    }

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            selectedPerson = state.selectedPerson,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            onNavigateToTransactions = onNavigateToTransactions?.let { navCallback ->
                { person ->
                    navCallback(person)
                    onDismiss()
                }
            },
            setPerson = { viewModel.onIntent(PersonIntent.SelectedPerson(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedPerson != null) {
        DeletePersonBottomSheet(
            person = state.selectedPerson!!,
            onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
            deleted = { viewModel.onIntent(PersonIntent.ResetFlags) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonManageBottomSheet(
    keyViewmodel: String = "PersonManageBottomSheet",
    viewModel: PersonViewModel = koinViewModel(key = keyViewmodel),
    isDeleteShow: Boolean = true,
    isEditShow: Boolean = true,
    clickable: Boolean = false,
    onPersonClick: (Person) -> Unit = {},
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Person) -> Unit)? = null
) = PersonPickerSingleBottomSheet(
    viewModel = viewModel,
    onPersonClick = onPersonClick,
    onDismiss = onDismiss,
    onNavigateToTransactions = onNavigateToTransactions,
    isEditShow = isEditShow,
    showEditButton = false
)

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
