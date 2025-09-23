package com.kazemieh.financialsource.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceListBottomSheet(
    viewModel: FinancialSourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit,
    onSourceClick: (id: Int, name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {

        Column(Modifier.fillMaxWidth().padding(16.dp)) {


            LazyColumn {
                items(state.financialSource) { financialSource ->
                    Text(
                        text = financialSource.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                financialSource.id?.toInt()
                                    ?.let { onSourceClick(it, financialSource.name) }
                            }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FloatingActionButton(
                onClick = onAddSourceClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن منبع "
                )
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceList(
    viewModel: FinancialSourceViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        items(state.financialSource) { source ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(source.name)
                    Text("${source.balance} T")
                }
            }
        }
    }
}
