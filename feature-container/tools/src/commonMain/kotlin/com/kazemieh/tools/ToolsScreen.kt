package com.kazemieh.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.glass.ScreenHeader
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToolsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToBudget: () -> Unit,
    onNavigateToInstallment: () -> Unit,
    onNavigateToPerson: () -> Unit,
    onNavigateToDebt: () -> Unit,
    onNavigateToCheck: () -> Unit,
    onNavigateToFixedExpense: () -> Unit,
    onNavigateToAssets: () -> Unit
) {
    val space = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(Res.string.navigation_tools),
                sub = stringResource(Res.string.title_tools_management)
            )

            Button(
                onClick = onNavigateToBudget,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.label_budgets))
            }

            Button(
                onClick = onNavigateToInstallment,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.navigation_installment))
            }

            Button(
                onClick = onNavigateToPerson,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.persons))
            }

            Button(
                onClick = onNavigateToDebt,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.navigation_debts))
            }

            Button(
                onClick = onNavigateToCheck,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.title_check_management))
            }

            Button(
                onClick = onNavigateToFixedExpense,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.title_fixed_expense_management))
            }

            Button(
                onClick = onNavigateToAssets,
                modifier = Modifier.padding(space.large)
            ) {
                FintrackBodyLargeText(stringResource(Res.string.title_assets_management))
            }
        }
    }
}
