package com.kazemieh.dashboard


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.FAB
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.financialsource.ui.SourceList
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.main.TotalTransactionCard
import com.kazemieh.transaction.ui.main.TransactionListScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = space.large)
        ) {
            item { Spacer(modifier = Modifier.height(space.mediumSmall)) }

            item { TotalTransactionCard(enableAnimationChart = state.enableAnimationChart) }

            item { Spacer(Modifier.height(space.large)) }

            item { SourceList { viewModel.onIntent(DashboardIntent.ShowAddSource) } }

            item { Spacer(Modifier.height(space.large)) }

            item { TransactionListScreen() }
        }

        FAB { viewModel.onIntent(DashboardIntent.ShowAddTransaction) }

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowAddTransaction) },
                transactionAdded = { viewModel.onIntent(DashboardIntent.AnimationEnabled) },
            )
        }

        /* if (state.showAddSource) {
             AddSourceBottomSheet(
                 onDismiss = { viewModel.onIntent(DashboardIntent.ShowAddTransaction) }
             )
         }*/

    }
}

