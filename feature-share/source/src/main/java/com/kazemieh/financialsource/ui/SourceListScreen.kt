package com.kazemieh.financialsource.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.toItemUi
import com.kazemieh.common.model.toSource
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.list.normal.ListBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceListBottomSheet(
    viewModel: SourceViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onSourceClick: (Source) -> Unit,
    onDismiss: () -> Unit,
) {

    LaunchedEffect(true) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
    }

    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SourceEffect.AddedSource -> onSourceClick(effect.source)
                SourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ListBottomSheet(
        title = stringResource(R.string.source),
        items = state.items,
        onConfirm = { viewModel.onIntent(SourceIntent.SelectedSource(it.toSource())) },
        onAddClick = { viewModel.onIntent(SourceIntent.OnAddSourceClick) },
        onDismiss = { viewModel.onIntent(SourceIntent.OnDismiss) },
        content = { item ->
            FintrackBodySmallText(
                text = stringResource(
                    R.string.balance,
                    item.extraData as String
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )

    if (state.isAddShow) {
        AddSourceBottomSheet(
            snackbarHostState = snackbarHostState,
            onDismiss = { viewModel.onIntent(SourceIntent.OnAddSourceClick) },
            setSource = { viewModel.onIntent(SourceIntent.SelectedSource(it)) }
        )
    }

}


@Composable
fun SourceListSelectionBottomSheet(
    viewModel: SourceViewModel = koinViewModel(),
    initialSelectionPairs: Set<Source> = emptySet(),
    onConfirmPairs: (Set<Source>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
    }

    val initialSelectionIds = initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.source),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toSource() }.toSet(), isAll)
        },
        onDismiss = onDismiss
    )
}


@Composable
fun SourceList(
    viewModel: SourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit
) {
    val space = LocalSpacing.current
    LaunchedEffect(true) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
    }

    val state by viewModel.state.collectAsState()
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackTitleMediumText(
                text = stringResource(R.string.financial_sources),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = onAddSourceClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_source),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {

            items(state.sources) { source ->
                Card(
                    modifier = Modifier.padding(horizontal = space.small),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = space.one)
                ) {
                    Column(
                        modifier = Modifier.padding(space.large)
                    ) {
                        FintrackBodyMediumText(text = source.name)
                        FintrackBodySmallText(
                            text = stringResource(R.string.balance, source.formattedBalance),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        }
    }
}

