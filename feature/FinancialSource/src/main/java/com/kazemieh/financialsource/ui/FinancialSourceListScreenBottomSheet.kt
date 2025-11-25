package com.kazemieh.financialsource.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.financialsource.R
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            LazyColumn {

                if (state.sources.isNotEmpty())
                    items(state.sources) { source ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSourceClick(source.id.toInt(), source.name)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                FintrackBodyMediumText(text = source.name)
                                FintrackBodySmallText(
                                    text = stringResource(
                                        R.string.balance,
                                        source.formattedBalance
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                else item { EmptyListScreen() }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                FloatingActionButton(
                    onClick = onAddSourceClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_source)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceListSelectionBottomSheet(
    viewModel: FinancialSourceViewModel = koinViewModel(),
    onSourceClick: (Set<Pair<Int, String>>) -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(true) {
        viewModel.onIntent(FinancialSourceIntent.LoadAllFinancialSource)
    }

    val state by viewModel.state.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            LazyColumn {
                if (state.sources.isNotEmpty())
                    items(state.sources) { source ->
                        val isSelected =
                            state.selectedSources?.contains(source.id.toInt() to source.name) == true
//                    val isSelected = state.selectedSources?.any { it.first == source.id.toInt() } == true
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.onIntent(FinancialSourceIntent.SelectedSources(source.id.toInt() to source.name))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        viewModel.onIntent(
                                            FinancialSourceIntent.SelectedSources(
                                                source.id.toInt() to source.name
                                            )
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))

                                FintrackBodyMediumText(text = source.name)

                                Spacer(modifier = Modifier.width(4.dp))

                                FintrackBodySmallText(
                                    text = stringResource(
                                        R.string.balance,
                                        source.formattedBalance
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        }
                    }
                else item { EmptyListScreen() }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSourceClick(state.selectedSources ?: emptySet()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                FintrackBodyMediumText(
                    text = stringResource(R.string.confirm),
                    color = MaterialTheme.colorScheme.background
                )
            }


        }
    }
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
