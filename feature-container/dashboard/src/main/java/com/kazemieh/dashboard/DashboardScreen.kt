@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.kazemieh.dashboard


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.financialsource.ui.list.SourceList
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.main.TotalTransactionCard
import com.kazemieh.transaction.ui.main.TransactionItemsProvider
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedBoxWithConstraintsScope", "ConfigurationScreenWidthHeight")
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    val listState = rememberLazyListState()

    var transactionItemsScope by remember { mutableStateOf<LazyListScope.() -> Unit>({}) }

    TransactionItemsProvider(
        onEdit = { transactionWithRelations ->
            viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet(transactionWithRelations))
        },
        onDelete = { transactionWithRelations ->
            viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet(transactionWithRelations))
        },
        onItemsReady = { scope -> transactionItemsScope = scope }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large, vertical = space.mediumSmall)
        ) {

            TotalTransactionCard(enableAnimationChart = state.enableAnimationChart)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {

                item { SourceList { viewModel.onIntent(DashboardIntent.ShowAddSource) } }

                item { Spacer(Modifier.height(space.mediumSmall)) }

                transactionItemsScope()
            }


        }

        FAB { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet()) }

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet()) },
                transactionAdded = { viewModel.onIntent(DashboardIntent.AnimationEnabled) },
            )
        }

        if (state.showDeleteTransaction) {
            DeleteTransactionBottomSheet(
                snackbarHostState = snackbarHostState,
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
                transactionDeleted = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
            )
        }

        if (state.showAddSource) {
            AddSourceBottomSheet(
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowAddSource) },
                setSource = { viewModel.onIntent(DashboardIntent.ShowAddSource) }
            )
        }

    }
}

