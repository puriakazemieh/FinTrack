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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.designsystem.component.list.normal.ListBottomSheet
import com.kazemieh.designsystem.component.list.selectable.SelectableListBottomSheet
import com.kazemieh.designsystem.component.list.toPairSetFrom
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceListBottomSheet(
    viewModel: FinancialSourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit,
    onSourceClick: (id: Int, name: String) -> Unit,
    onDismiss: () -> Unit,
) {

    LaunchedEffect(true) {
        viewModel.onIntent(FinancialSourceIntent.LoadAllFinancialSource)
    }

    val state by viewModel.state.collectAsState()


    val items = state.sources.map { ItemUi(it.id.toInt(), it.name, it.formattedBalance) }

    ListBottomSheet(
        title = stringResource(R.string.source),
        items = items,
        onConfirm = onSourceClick,
        onAddClick = onAddSourceClick,
        onDismiss = onDismiss,
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
}


@Composable
fun SourceList(
    viewModel: FinancialSourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit
) {
    LaunchedEffect(true) {
        viewModel.onIntent(FinancialSourceIntent.LoadAllFinancialSource)
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
//        item {
//            Spacer(modifier = Modifier.width(4.dp))
//        }

            items(state.sources) { source ->
                Card(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        FintrackBodyMediumText(text = source.name)
                        FintrackBodySmallText(
                            text = stringResource(R.string.balance, source.formattedBalance),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

//        item {
//            Spacer(modifier = Modifier.width(4.dp))
//        }
        }
    }
}


@Composable
fun SourceListSelectionBottomSheet(
    viewModel: FinancialSourceViewModel = koinViewModel(),
    initialSelectionPairs: Set<Pair<Int, String>> = emptySet(),
    onConfirmPairs: (Set<Pair<Int, String>>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.onIntent(FinancialSourceIntent.LoadAllFinancialSource)
    }

    val initialSelectionIds = initialSelectionPairs.map { it.first }.toSet()

    SelectableListBottomSheet(
        title = stringResource(R.string.source),
        items = state.items,
        initialSelection = initialSelectionIds,
        onConfirm = { selectedIds, isAll ->
            val pairSet = selectedIds.toPairSetFrom(state.items)
            onConfirmPairs(pairSet, isAll)
        },
        onDismiss = onDismiss
    )
}