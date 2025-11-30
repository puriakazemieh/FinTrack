package com.kazemieh.person.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.designsystem.component.list.selectable.SelectableFlowRowBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.designsystem.component.list.toPairSetFrom
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonListBottomSheet(
    viewModel: PersonViewModel = koinViewModel(),
    selectedPersons: Set<Pair<Int, String>>?,
    onSubmitClick: (Set<Pair<Int, String>>?) -> Unit,
    onDismiss: () -> Unit
) {


    LaunchedEffect(true) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    LaunchedEffect(selectedPersons) {
        viewModel.onIntent(PersonIntent.SetAllSelectedPersons(selectedPersons))
    }

    val state by viewModel.state.collectAsState()

    SelectableFlowRowBottomSheet(
        title = stringResource(R.string.persons),
        items = state.items,
        initialSelection = state.initialSelectionIds,
        onConfirm = { selectedIds ->
            val pairSet = selectedIds.toPairSetFrom(state.items)
            onSubmitClick(pairSet)
        },
        onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
        onDismiss = onDismiss,
    )

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            onDismiss = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
            setPerson = { id, name -> viewModel.onIntent(PersonIntent.SetSelectedPerson(id to name)) }
        )
    }
}


@Composable
fun PersonListSelectionBottomSheet(
    viewModel: PersonViewModel = koinViewModel(),
    initialSelectionPairs: Set<Pair<Int, String>> = emptySet(),
    onConfirmPairs: (Set<Pair<Int, String>>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }
    val items = state.persons.map { ItemUi(it.id?.toInt() ?: 0, it.name) }
    val initialSelectionIds = initialSelectionPairs.map { it.first }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.persons),
        items = items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedIds, isAll ->
            val pairSet = selectedIds.toPairSetFrom(items)
            onConfirmPairs(pairSet, isAll)
        },
        onDismiss = onDismiss
    )
}